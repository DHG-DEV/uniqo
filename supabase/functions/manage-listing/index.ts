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

    const body = await req.json();

    if (body.action === "create") {
      const { title, price, category, subCategory, condition, description, imageUrl, latitude, longitude, address } = body;
      const { data, error } = await supabase.from("listings").insert({
        title, price, category, sub_category: subCategory, condition, description,
        image_url: imageUrl, seller_id: userId, latitude, longitude, address
      }).select("id").single();
      if (error) return json({ error: "Could not create listing." }, 500);
      return json({ id: data.id });
    }

    if (body.action === "update") {
      const { listingId, ...fields } = body;
      const { data: listing } = await supabase.from("listings").select("seller_id").eq("id", listingId).maybeSingle();
      if (!listing || listing.seller_id !== userId) return json({ error: "Not authorized to edit this listing." }, 403);

      const { error } = await supabase.from("listings").update(fields).eq("id", listingId);
      if (error) return json({ error: "Could not update listing." }, 500);
      return json({ success: true });
    }

    if (body.action === "delete") {
      const { listingId } = body;
      const { data: listing } = await supabase.from("listings").select("seller_id").eq("id", listingId).maybeSingle();
      if (!listing || listing.seller_id !== userId) return json({ error: "Not authorized to delete this listing." }, 403);

      const { error } = await supabase.from("listings").delete().eq("id", listingId);
      if (error) return json({ error: "Could not delete listing." }, 500);
      return json({ success: true });
    }

    return json({ error: "Unknown action." }, 400);
  } catch {
    return json({ error: "Something went wrong." }, 500);
  }
});

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}