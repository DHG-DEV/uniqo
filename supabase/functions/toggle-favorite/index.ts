import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { verify as verifyJwt } from "https://deno.land/x/djwt@v3.0.2/mod.ts";

const supabase = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
const SUPABASE_JWT_SECRET = Deno.env.get("APP_SUPABASE_JWT_SECRET")!;

async function getUserIdFromToken(authHeader: string | null): Promise<string | null> {
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.slice(7);
  try {
    const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(SUPABASE_JWT_SECRET), { name: "HMAC", hash: "SHA-256" }, false, ["verify"]);
    const payload = await verifyJwt(token, key);
    return (payload as { sub: string }).sub;
  } catch {
    return null;
  }
}

serve(async (req) => {
  try {
    const userId = await getUserIdFromToken(req.headers.get("Authorization"));
    if (!userId) return json({ error: "Not authenticated." }, 401);

    const { listingId } = await req.json();
    const { data: existing } = await supabase.from("favorites").select("*").eq("user_id", userId).eq("listing_id", listingId).maybeSingle();

    if (existing) {
      await supabase.from("favorites").delete().eq("user_id", userId).eq("listing_id", listingId);
      return json({ favorited: false });
    } else {
      await supabase.from("favorites").insert({ user_id: userId, listing_id: listingId });
      return json({ favorited: true });
    }
  } catch {
    return json({ error: "Something went wrong." }, 500);
  }
});

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}