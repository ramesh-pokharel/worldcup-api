-- V10__fix_match_schedule.sql
-- Source: official FIFA.com 2026 World Cup schedule
-- All times expressed as Eastern Daylight Time (EDT = UTC-4) and converted to UTC.
--
-- Group stage: 2 time corrections
--   Match 31 (BRA vs HTI) : openfootball had 21:00 EDT; FIFA shows 20:30 EDT → 00:30 UTC
--   Match 32 (TUR vs PRY) : openfootball had 00:00 EDT; FIFA shows 23:00 EDT prev day → 03:00 UTC
--
-- Knockout stage (matches 73-104): complete schedule replacement
--   V5 used a speculative July schedule; FIFA has R32 starting 28 June.
--   Both scheduled_at and stadium_id are corrected for all 32 knockout matches.

-- ---------------------------------------------------------------
-- PART 1 – Group stage time corrections (stadium unchanged)
-- ---------------------------------------------------------------
UPDATE matches SET scheduled_at = '2026-06-20T00:30:00Z' WHERE match_number = 31;  -- BRA vs HTI (20:30 EDT)
UPDATE matches SET scheduled_at = '2026-06-20T03:00:00Z' WHERE match_number = 32;  -- TUR vs PRY (23:00 EDT)

-- ---------------------------------------------------------------
-- PART 2 – Knockout stage: correct scheduled_at + stadium_id
--
-- Uses a single UPDATE via a VALUES CTE so every match is fixed
-- in one pass rather than 32 individual statements.
-- ---------------------------------------------------------------
WITH corrections (num, sch, sname) AS (VALUES
  -- ===== ROUND OF 32  (June 28 – July 3) =====
  ( 73, '2026-06-28T19:00:00Z', 'SoFi Stadium'),            -- Sun 28 Jun  15:00 EDT  Los Angeles
  ( 74, '2026-06-29T17:00:00Z', 'NRG Stadium'),             -- Mon 29 Jun  13:00 EDT  Houston
  ( 75, '2026-06-29T20:30:00Z', 'Gillette Stadium'),        -- Mon 29 Jun  16:30 EDT  Boston
  ( 76, '2026-06-30T01:00:00Z', 'Estadio BBVA'),            -- Mon 29 Jun  21:00 EDT  Monterrey
  ( 77, '2026-06-30T17:00:00Z', 'AT&T Stadium'),            -- Tue 30 Jun  13:00 EDT  Dallas
  ( 78, '2026-06-30T21:00:00Z', 'MetLife Stadium'),         -- Tue 30 Jun  17:00 EDT  New York/NJ
  ( 79, '2026-07-01T01:00:00Z', 'Estadio Azteca'),          -- Tue 30 Jun  21:00 EDT  Mexico City
  ( 80, '2026-07-01T16:00:00Z', 'Mercedes-Benz Stadium'),   -- Wed 01 Jul  12:00 EDT  Atlanta
  ( 81, '2026-07-01T20:00:00Z', 'Lumen Field'),             -- Wed 01 Jul  16:00 EDT  Seattle
  ( 82, '2026-07-02T00:00:00Z', 'Levi''s Stadium'),         -- Wed 01 Jul  20:00 EDT  SF Bay Area
  ( 83, '2026-07-02T19:00:00Z', 'SoFi Stadium'),            -- Thu 02 Jul  15:00 EDT  Los Angeles
  ( 84, '2026-07-02T23:00:00Z', 'BMO Field'),               -- Thu 02 Jul  19:00 EDT  Toronto
  ( 85, '2026-07-03T03:00:00Z', 'BC Place'),                -- Thu 02 Jul  23:00 EDT  Vancouver
  ( 86, '2026-07-03T18:00:00Z', 'AT&T Stadium'),            -- Fri 03 Jul  14:00 EDT  Dallas
  ( 87, '2026-07-03T22:00:00Z', 'Hard Rock Stadium'),       -- Fri 03 Jul  18:00 EDT  Miami
  ( 88, '2026-07-04T01:30:00Z', 'Arrowhead Stadium'),       -- Fri 03 Jul  21:30 EDT  Kansas City

  -- ===== ROUND OF 16  (July 4 – 7) =====
  ( 89, '2026-07-04T17:00:00Z', 'NRG Stadium'),             -- Sat 04 Jul  13:00 EDT  Houston
  ( 90, '2026-07-04T21:00:00Z', 'Lincoln Financial Field'), -- Sat 04 Jul  17:00 EDT  Philadelphia
  ( 91, '2026-07-05T20:00:00Z', 'MetLife Stadium'),         -- Sun 05 Jul  16:00 EDT  New York/NJ
  ( 92, '2026-07-06T00:00:00Z', 'Estadio Azteca'),          -- Sun 05 Jul  20:00 EDT  Mexico City
  ( 93, '2026-07-06T19:00:00Z', 'AT&T Stadium'),            -- Mon 06 Jul  15:00 EDT  Dallas
  ( 94, '2026-07-07T00:00:00Z', 'Lumen Field'),             -- Mon 06 Jul  20:00 EDT  Seattle
  ( 95, '2026-07-07T16:00:00Z', 'Mercedes-Benz Stadium'),   -- Tue 07 Jul  12:00 EDT  Atlanta
  ( 96, '2026-07-07T20:00:00Z', 'BC Place'),                -- Tue 07 Jul  16:00 EDT  Vancouver

  -- ===== QUARTER-FINALS  (July 9 – 11) =====
  ( 97, '2026-07-09T20:00:00Z', 'Gillette Stadium'),        -- Thu 09 Jul  16:00 EDT  Boston
  ( 98, '2026-07-10T19:00:00Z', 'SoFi Stadium'),            -- Fri 10 Jul  15:00 EDT  Los Angeles
  ( 99, '2026-07-11T21:00:00Z', 'Hard Rock Stadium'),       -- Sat 11 Jul  17:00 EDT  Miami
  (100, '2026-07-12T01:00:00Z', 'Arrowhead Stadium'),       -- Sat 11 Jul  21:00 EDT  Kansas City

  -- ===== SEMI-FINALS  (July 14 – 15) =====
  (101, '2026-07-14T19:00:00Z', 'AT&T Stadium'),            -- Tue 14 Jul  15:00 EDT  Dallas
  (102, '2026-07-15T19:00:00Z', 'Mercedes-Benz Stadium'),   -- Wed 15 Jul  15:00 EDT  Atlanta

  -- ===== THIRD PLACE & FINAL =====
  (103, '2026-07-18T21:00:00Z', 'Hard Rock Stadium'),       -- Sat 18 Jul  17:00 EDT  Miami
  (104, '2026-07-19T19:00:00Z', 'MetLife Stadium')          -- Sun 19 Jul  15:00 EDT  New York/NJ
)
UPDATE matches m
SET scheduled_at = c.sch::timestamptz,
    stadium_id   = s.id
FROM corrections c
JOIN stadiums s ON s.name = c.sname
WHERE m.match_number = c.num;
