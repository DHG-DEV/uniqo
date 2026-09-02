import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import * as bcrypt from "https://deno.land/x/bcrypt@v0.4.1/mod.ts";
import { create as createJwt } from "https://deno.land/x/djwt@v3.0.2/mod.ts";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

const SUPABASE_JWT_SECRET = Deno.env.get("APP_SUPABASE_JWT_SECRET")!;

serve(async (req) => {
  try {
    const { username, password } = await req.json();

    if (!username || !password) {
      return json({ error: "Invalid username or password." }, 400);
    }

    const { data: user } = await supabase
      .from("app_users")
      .select("id, password_hash, mobile_verified")
      .eq("username", username)
      .maybeSingle();

    const dummyHash =
      "$2a$10$CwTycUXWue0Thq9StjUM0uJ8Kk6ZekL/Rq0bZ.5Ip4gKfL2mVy5Iy";

    const passwordOk = user
      ? bcrypt.compareSync(password, user.password_hash)
      : bcrypt.compareSync(password, dummyHash);

    if (!user || !passwordOk || !user.mobile_verified) {
      return json({ error: "Invalid username or password." }, 401);
    }

    const expiresIn = 60 * 60 * 24 * 7;
    const now = Math.floor(Date.now() / 1000);

    const key = await crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(SUPABASE_JWT_SECRET),
      {
        name: "HMAC",
        hash: "SHA-256"
      },
      false,
      ["sign"]
    );

    const token = await createJwt(
      {
        alg: "HS256",
        typ: "JWT"
      },
      {
        aud: "authenticated",
        role: "authenticated",
        sub: user.id,
        iat: now,
        exp: now + expiresIn
      },
      key
    );

    return json({
      token,
      userId: user.id,
      expiresIn
    });
  } catch (error) {
    console.error("login error:", error);
    return json({ error: "Invalid username or password." }, 500);
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