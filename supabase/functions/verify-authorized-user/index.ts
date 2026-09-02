import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

serve(async (req) => {
  try {
    const { serialNumber, dob } = await req.json();

    if (!serialNumber || !dob) {
      return json(
        { error: "Serial number and date of birth are required." },
        400
      );
    }

    // Normalize serial number.
    const normalizedSerial = String(serialNumber)
      .trim()
      .replace(/\s+/g, "")
      .toUpperCase();

    // Normalize DOB to YYYY-MM-DD.
    const normalizedDob = normalizeDob(String(dob));

    if (!normalizedDob) {
      return json(
        { error: "Invalid date of birth format." },
        400
      );
    }

    const { data: row, error } = await supabase
      .from("authorized_users")
      .select("id, registered, dob")
      .eq("serial_number", normalizedSerial)
      .maybeSingle();

    if (error) {
      console.error("authorized_users query error:", error);

      return json(
        {
          error: "Database verification failed.",
          details: error.message
        },
        500
      );
    }

    if (!row) {
      return json(
        {
          error:
            "The details could not be verified. Please check your serial number."
        },
        404
      );
    }

    const databaseDob = normalizeDob(String(row.dob));

    if (!databaseDob || databaseDob !== normalizedDob) {
      return json(
        {
          error:
            "The details could not be verified. Please check your information."
        },
        404
      );
    }

    if (row.registered) {
      return json(
        {
          error: "This serial number has already been registered."
        },
        409
      );
    }

    const verifyToken = crypto.randomUUID();

    const { error: updateError } = await supabase
      .from("authorized_users")
      .update({
        verify_token: verifyToken,
        verify_token_expires: new Date(
          Date.now() + 10 * 60 * 1000
        ).toISOString()
      })
      .eq("id", row.id);

    if (updateError) {
      console.error("verify token update error:", updateError);

      return json(
        {
          error: "Could not start verification.",
          details: updateError.message
        },
        500
      );
    }

    return json({
      verified: true,
      authorizedUserId: row.id,
      verifyToken
    });

  } catch (error) {
    console.error("verify-authorized-user error:", error);

    return json(
      {
        error: "Something went wrong.",
        details: error instanceof Error ? error.message : String(error)
      },
      500
    );
  }
});

function normalizeDob(value: string): string | null {
  const input = value.trim();

  // Already YYYY-MM-DD.
  if (/^\d{4}-\d{2}-\d{2}$/.test(input)) {
    return input;
  }

  // DD/MM/YYYY.
  const slash = input.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);

  if (slash) {
    const [, day, month, year] = slash;
    return `${year}-${month}-${day}`;
  }

  // DD-MM-YYYY.
  const dash = input.match(/^(\d{2})-(\d{2})-(\d{4})$/);

  if (dash) {
    const [, day, month, year] = dash;
    return `${year}-${month}-${day}`;
  }

  return null;
}

function json(body: unknown, status = 200) {
  return new Response(
    JSON.stringify(body),
    {
      status,
      headers: {
        "Content-Type": "application/json"
      }
    }
  );
}