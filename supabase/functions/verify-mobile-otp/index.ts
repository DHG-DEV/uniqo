import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import * as bcrypt from "https://deno.land/x/bcrypt@v0.4.1/mod.ts";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

serve(async (req) => {
  try {
    const { userId, code } = await req.json();

    if (!userId || !code) {
      return json({ error: "Missing fields." }, 400);
    }

    const { data: otp } = await supabase
      .from("otp_codes")
      .select("*")
      .eq("app_user_id", userId)
      .eq("purpose", "signup_mobile")
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

    await supabase
      .from("otp_codes")
      .update({
        consumed: true
      })
      .eq("id", otp.id);

    const { data: appUser } = await supabase
      .from("app_users")
      .select("authorized_user_id")
      .eq("id", userId)
      .single();

    if (!appUser?.authorized_user_id) {
      return json({ error: "Account authorization record not found." }, 500);
    }

    await supabase
      .from("app_users")
      .update({
        mobile_verified: true
      })
      .eq("id", userId);

    await supabase
      .from("authorized_users")
      .update({
        registered: true,
        user_id: userId
      })
      .eq("id", appUser.authorized_user_id);

    const { data: authUserRecord } = await supabase
      .from("authorized_users")
      .select("full_name")
      .eq("id", appUser.authorized_user_id)
      .maybeSingle();

    await supabase
      .from("profiles")
      .upsert({
        id: userId,
        name: authUserRecord?.full_name ?? "Student",
        college: "",
        course: "",
        year: "",
        email_verified: true,
        student_verified: true,
        phone_verified: true
      });

    return json({ verified: true });
  } catch (error) {
    console.error("verify-mobile-otp error:", error);
    return json({ error: "Something went wrong." }, 500);
  }
});

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json"
    }
  });
}