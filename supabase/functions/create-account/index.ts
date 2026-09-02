import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import * as bcrypt from "https://deno.land/x/bcrypt@v0.4.1/mod.ts";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

serve(async (req) => {
  try {
    const { authorizedUserId, verifyToken, username, password, mobileNumber } = await req.json();

    if (!authorizedUserId || !verifyToken || !username || !password || !mobileNumber) {
      return json({ error: "All fields are required." }, 400);
    }

    if (password.length < 8) {
      return json({ error: "Password must be at least 8 characters." }, 400);
    }

    const { data: authUser } = await supabase
      .from("authorized_users")
      .select("id, registered, verify_token, verify_token_expires")
      .eq("id", authorizedUserId)
      .maybeSingle();

    if (
      !authUser ||
      authUser.verify_token !== verifyToken ||
      new Date(authUser.verify_token_expires) < new Date()
    ) {
      return json({ error: "Verification expired. Please start again." }, 401);
    }

    if (authUser.registered) {
      return json({ error: "This serial number has already been registered." }, 409);
    }

    const { data: existingUsername } = await supabase
      .from("app_users")
      .select("id")
      .eq("username", username)
      .maybeSingle();

    if (existingUsername) {
      return json({ error: "That username is taken." }, 409);
    }

    const passwordHash = bcrypt.hashSync(password);

    const { data: newUser, error: insertErr } = await supabase
      .from("app_users")
      .insert({
        username,
        password_hash: passwordHash,
        mobile_number: mobileNumber,
        authorized_user_id: authorizedUserId
      })
      .select("id")
      .single();

    if (insertErr) {
      console.error("app_users insert error:", insertErr);
      return json({ error: "Could not create account." }, 500);
    }

    const rateLimitOk = await checkAndBumpRateLimit(mobileNumber);

    if (!rateLimitOk) {
      return json({ error: "Too many attempts. Try again later." }, 429);
    }

    const otpCode = generateOtp();
    const codeHash = bcrypt.hashSync(otpCode);

    const { error: otpInsertErr } = await supabase
      .from("otp_codes")
      .insert({
        purpose: "signup_mobile",
        target_mobile: mobileNumber,
        app_user_id: newUser.id,
        code_hash: codeHash,
        expires_at: new Date(Date.now() + 5 * 60 * 1000).toISOString()
      });

    if (otpInsertErr) {
      console.error("otp_codes insert error:", otpInsertErr);
      return json({ error: "Could not create verification code." }, 500);
    }

    console.log(`[DEV OTP] signup OTP for ${mobileNumber}: ${otpCode}`);

    return json({
      userId: newUser.id,
      otpSent: true
    });
  } catch (error) {
    console.error("create-account error:", error);
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
    now.getTime() - new Date(data.window_started_at).getTime() > 60 * 60 * 1000
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