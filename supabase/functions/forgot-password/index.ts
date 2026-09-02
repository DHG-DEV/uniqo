import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import * as bcrypt from "https://deno.land/x/bcrypt@v0.4.1/mod.ts";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

serve(async (req) => {
  try {
    const body = await req.json();

    if (body.action === "request") {
      const { username } = body;

      const { data: user } = await supabase
        .from("app_users")
        .select("id, mobile_number, mobile_verified")
        .eq("username", username)
        .maybeSingle();

      if (user && user.mobile_verified) {
        const rateLimitOk = await checkAndBumpRateLimit(user.mobile_number);

        if (rateLimitOk) {
          const otpCode = generateOtp();
          const codeHash = bcrypt.hashSync(otpCode);

          const { error } = await supabase
            .from("otp_codes")
            .insert({
              purpose: "password_reset",
              target_mobile: user.mobile_number,
              app_user_id: user.id,
              code_hash: codeHash,
              expires_at: new Date(Date.now() + 5 * 60 * 1000).toISOString()
            });

          if (error) {
            console.error("password reset OTP insert error:", error);
          } else {
            console.log(
              `[DEV OTP] password reset OTP for ${user.mobile_number}: ${otpCode}`
            );
          }
        }
      }

      return json({
        message:
          "If that account exists, an OTP has been sent to the registered mobile number."
      });
    }

    if (body.action === "verify") {
      const { username, code } = body;

      const { data: user } = await supabase
        .from("app_users")
        .select("id")
        .eq("username", username)
        .maybeSingle();

      if (!user) {
        return json({ error: "Incorrect OTP." }, 401);
      }

      const { data: otp } = await supabase
        .from("otp_codes")
        .select("*")
        .eq("app_user_id", user.id)
        .eq("purpose", "password_reset")
        .eq("consumed", false)
        .order("created_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      if (!otp) {
        return json({ error: "No active OTP. Request a new one." }, 404);
      }

      if (new Date(otp.expires_at) < new Date()) {
        return json({ error: "OTP expired. Request a new one." }, 410);
      }

      if (otp.attempt_count >= otp.max_attempts) {
        return json({ error: "Too many attempts. Request a new OTP." }, 429);
      }

      const valid = bcrypt.compareSync(code, otp.code_hash);

      if (!valid) {
        await supabase
          .from("otp_codes")
          .update({
            attempt_count: otp.attempt_count + 1
          })
          .eq("id", otp.id);

        return json({ error: "Incorrect OTP." }, 401);
      }

      const resetToken = crypto.randomUUID();

      await supabase
        .from("password_reset_sessions")
        .insert({
          app_user_id: user.id,
          otp_id: otp.id,
          verified: true,
          expires_at: new Date(Date.now() + 10 * 60 * 1000).toISOString()
        });

      await supabase
        .from("otp_codes")
        .update({
          consumed: true
        })
        .eq("id", otp.id);

      return json({
        verified: true,
        resetToken,
        userId: user.id
      });
    }

    if (body.action === "reset") {
      const { userId, newPassword } = body;

      if (!newPassword || newPassword.length < 8) {
        return json(
          { error: "Password must be at least 8 characters." },
          400
        );
      }

      const { data: session } = await supabase
        .from("password_reset_sessions")
        .select("*")
        .eq("app_user_id", userId)
        .eq("verified", true)
        .order("created_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      if (!session || new Date(session.expires_at) < new Date()) {
        return json(
          { error: "Reset session expired. Start again." },
          401
        );
      }

      const passwordHash = bcrypt.hashSync(newPassword);

      const { error: updateError } = await supabase
        .from("app_users")
        .update({
          password_hash: passwordHash
        })
        .eq("id", userId);

      if (updateError) {
        console.error("password update error:", updateError);
        return json({ error: "Could not reset password." }, 500);
      }

      await supabase
        .from("password_reset_sessions")
        .delete()
        .eq("id", session.id);

      return json({ success: true });
    }

    return json({ error: "Unknown action." }, 400);
  } catch (error) {
    console.error("forgot-password error:", error);
    return json({ error: "Something went wrong." }, 500);
  }
});

function generateOtp(): string {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

async function checkAndBumpRateLimit(mobile: string): Promise<boolean> {
  const { data } = await supabase
    .from("otp_rate_limits")
    .select("*")
    .eq("target_mobile", mobile)
    .maybeSingle();

  const now = new Date();

  if (
    !data ||
    now.getTime() - new Date(data.window_started_at).getTime() >
      60 * 60 * 1000
  ) {
    await supabase
      .from("otp_rate_limits")
      .upsert({
        target_mobile: mobile,
        request_count: 1,
        window_started_at: now.toISOString()
      });

    return true;
  }

  if (data.request_count >= 5) {
    return false;
  }

  await supabase
    .from("otp_rate_limits")
    .update({
      request_count: data.request_count + 1
    })
    .eq("target_mobile", mobile);

  return true;
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json"
    }
  });
}