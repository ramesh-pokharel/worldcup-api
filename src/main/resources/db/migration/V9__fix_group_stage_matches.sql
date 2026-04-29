-- V9__fix_group_stage_matches.sql
-- Source: openfootball/worldcup.json (github.com/openfootball/worldcup.json)
--
-- WHY this migration is needed:
--   V5 used pre-draw fixtures (wrong teams, dates, venues).
--   V8 NULLed the 13 removed teams but left 72 stale match rows.
--   This migration replaces all 72 group-stage rows with the official
--   December 2025 FIFA draw pairings, venues, and UTC kick-off times.
--
-- WHAT changes:
--   1. Allegiant Stadium (Las Vegas — not a 2026 venue) → Mercedes-Benz Stadium (Atlanta).
--   2. Cascade-safe removal of predictions / events / comments on matches 1–72.
--   3. DELETE matches 1–72 and INSERT the correct 72 rows.
--
-- UTC conversion used for each entry:
--   UTC-4 (EDT / Toronto / Miami / Philadelphia / NY-NJ / Boston / Atlanta) : local + 4 h
--   UTC-5 (CDT / Houston / Dallas / Kansas City) : local + 5 h
--   UTC-6 (Mexico, no DST since 2023 / Guadalajara / Monterrey) : local + 6 h
--   UTC-7 (PDT / Los Angeles / Seattle / Vancouver / San Francisco) : local + 7 h

-- ---------------------------------------------------------------
-- STEP 1 – Fix the stadium: Las Vegas → Atlanta
-- ---------------------------------------------------------------
UPDATE stadiums
SET name      = 'Mercedes-Benz Stadium',
    city      = 'Atlanta, GA',
    capacity  = 71000,
    latitude  =  33.7553,
    longitude = -84.4006
WHERE name = 'Allegiant Stadium';

-- ---------------------------------------------------------------
-- STEP 2 – Remove FK-constrained rows referencing matches 1–72
-- ---------------------------------------------------------------
DELETE FROM predictions
WHERE match_id IN (SELECT id FROM matches WHERE match_number BETWEEN 1 AND 72);

DELETE FROM match_events
WHERE match_id IN (SELECT id FROM matches WHERE match_number BETWEEN 1 AND 72);

DELETE FROM comments
WHERE match_id IN (SELECT id FROM matches WHERE match_number BETWEEN 1 AND 72);

-- ---------------------------------------------------------------
-- STEP 3 – Drop the stale group-stage rows
-- ---------------------------------------------------------------
DELETE FROM matches WHERE match_number BETWEEN 1 AND 72;

-- ---------------------------------------------------------------
-- STEP 4 – Insert all 72 group-stage matches
--
-- Match numbering follows chronological kick-off order (UTC):
--   1– 24  Matchdays 1–7   (all groups' first round)
--  25– 48  Matchdays 8–13  (all groups' second round)
--  49– 72  Matchdays 14–17 (all groups' third round — simultaneous
--                            pairs within the same group share the
--                            same scheduled_at timestamp)
-- ---------------------------------------------------------------
WITH
  tm AS (SELECT c.code_iso3, t.id AS tid
         FROM teams t JOIN countries c ON t.country_id = c.id),
  sm AS (SELECT name, id AS sid FROM stadiums)
INSERT INTO matches
  (match_number, stage, group_id,
   home_team_id, away_team_id,
   stadium_id, scheduled_at, status)
SELECT
  v.num,
  'GROUP'::match_stage,
  v.grp::char(1),
  ht.tid,
  at.tid,
  sd.sid,
  v.sch::timestamptz,
  'SCHEDULED'::match_status
FROM (VALUES

  -- ============================================================
  --  MATCHDAY 1  (June 11) — Group A
  -- ============================================================
  ( 1,'A','MEX','ZAF','Estadio Azteca',         '2026-06-11T19:00:00Z'),
  -- 13:00 CDT(-6) → 19:00 UTC
  ( 2,'A','KOR','CZE','Estadio Akron',           '2026-06-12T02:00:00Z'),
  -- 20:00 CDT(-6) → 02:00 UTC

  -- ============================================================
  --  MATCHDAY 2  (June 12) — Groups B · D
  -- ============================================================
  ( 3,'B','CAN','BIH','BMO Field',               '2026-06-12T19:00:00Z'),
  -- 15:00 EDT(-4) → 19:00 UTC
  ( 4,'D','USA','PRY','SoFi Stadium',            '2026-06-13T01:00:00Z'),
  -- 18:00 PDT(-7) → 01:00 UTC

  -- ============================================================
  --  MATCHDAY 3  (June 13) — Groups B · C · D
  -- ============================================================
  ( 5,'B','QAT','CHE','Levi''s Stadium',         '2026-06-13T19:00:00Z'),
  -- 12:00 PDT(-7) → 19:00 UTC
  ( 6,'C','BRA','MAR','MetLife Stadium',         '2026-06-13T22:00:00Z'),
  -- 18:00 EDT(-4) → 22:00 UTC
  ( 7,'C','HTI','SCO','Gillette Stadium',        '2026-06-14T01:00:00Z'),
  -- 21:00 EDT(-4) → 01:00 UTC
  ( 8,'D','AUS','TUR','BC Place',                '2026-06-14T04:00:00Z'),
  -- 21:00 PDT(-7) → 04:00 UTC

  -- ============================================================
  --  MATCHDAY 4  (June 14) — Groups E · F
  -- ============================================================
  ( 9,'E','DEU','CUW','NRG Stadium',             '2026-06-14T17:00:00Z'),
  -- 12:00 CDT(-5) → 17:00 UTC
  (10,'F','NLD','JPN','AT&T Stadium',            '2026-06-14T20:00:00Z'),
  -- 15:00 CDT(-5) → 20:00 UTC
  (11,'E','CIV','ECU','Lincoln Financial Field', '2026-06-14T23:00:00Z'),
  -- 19:00 EDT(-4) → 23:00 UTC
  (12,'F','SWE','TUN','Estadio BBVA',            '2026-06-15T02:00:00Z'),
  -- 20:00 CDT(-6) → 02:00 UTC

  -- ============================================================
  --  MATCHDAY 5  (June 15) — Groups G · H
  -- ============================================================
  (13,'H','ESP','CPV','Mercedes-Benz Stadium',   '2026-06-15T16:00:00Z'),
  -- 12:00 EDT(-4) → 16:00 UTC
  (14,'G','BEL','EGY','Lumen Field',             '2026-06-15T19:00:00Z'),
  -- 12:00 PDT(-7) → 19:00 UTC
  (15,'H','SAU','URY','Hard Rock Stadium',       '2026-06-15T22:00:00Z'),
  -- 18:00 EDT(-4) → 22:00 UTC
  (16,'G','IRN','NZL','SoFi Stadium',            '2026-06-16T01:00:00Z'),
  -- 18:00 PDT(-7) → 01:00 UTC

  -- ============================================================
  --  MATCHDAY 6  (June 16) — Groups I · J
  -- ============================================================
  (17,'I','FRA','SEN','MetLife Stadium',         '2026-06-16T19:00:00Z'),
  -- 15:00 EDT(-4) → 19:00 UTC
  (18,'I','IRQ','NOR','Gillette Stadium',        '2026-06-16T22:00:00Z'),
  -- 18:00 EDT(-4) → 22:00 UTC
  (19,'J','ARG','DZA','Arrowhead Stadium',       '2026-06-17T01:00:00Z'),
  -- 20:00 CDT(-5) → 01:00 UTC
  (20,'J','AUT','JOR','Levi''s Stadium',         '2026-06-17T04:00:00Z'),
  -- 21:00 PDT(-7) → 04:00 UTC

  -- ============================================================
  --  MATCHDAY 7  (June 17) — Groups K · L
  -- ============================================================
  (21,'K','PRT','COD','NRG Stadium',             '2026-06-17T17:00:00Z'),
  -- 12:00 CDT(-5) → 17:00 UTC
  (22,'L','ENG','HRV','AT&T Stadium',            '2026-06-17T20:00:00Z'),
  -- 15:00 CDT(-5) → 20:00 UTC
  (23,'L','GHA','PAN','BMO Field',               '2026-06-17T23:00:00Z'),
  -- 19:00 EDT(-4) → 23:00 UTC
  (24,'K','UZB','COL','Estadio Azteca',          '2026-06-18T02:00:00Z'),
  -- 20:00 CDT(-6) → 02:00 UTC

  -- ============================================================
  --  MATCHDAY 8  (June 18) — Groups A · B (MD2)
  -- ============================================================
  (25,'A','CZE','ZAF','Mercedes-Benz Stadium',   '2026-06-18T16:00:00Z'),
  -- 12:00 EDT(-4) → 16:00 UTC
  (26,'B','CHE','BIH','SoFi Stadium',            '2026-06-18T19:00:00Z'),
  -- 12:00 PDT(-7) → 19:00 UTC
  (27,'B','CAN','QAT','BC Place',                '2026-06-18T22:00:00Z'),
  -- 15:00 PDT(-7) → 22:00 UTC
  (28,'A','MEX','KOR','Estadio Akron',           '2026-06-19T01:00:00Z'),
  -- 19:00 CDT(-6) → 01:00 UTC

  -- ============================================================
  --  MATCHDAY 9  (June 19) — Groups C · D (MD2)
  -- ============================================================
  (29,'D','USA','AUS','Lumen Field',             '2026-06-19T19:00:00Z'),
  -- 12:00 PDT(-7) → 19:00 UTC
  (30,'C','SCO','MAR','Gillette Stadium',        '2026-06-19T22:00:00Z'),
  -- 18:00 EDT(-4) → 22:00 UTC
  (31,'C','BRA','HTI','Lincoln Financial Field', '2026-06-20T01:00:00Z'),
  -- 21:00 EDT(-4) → 01:00 UTC
  (32,'D','TUR','PRY','Levi''s Stadium',         '2026-06-20T04:00:00Z'),
  -- 21:00 PDT(-7) → 04:00 UTC

  -- ============================================================
  --  MATCHDAY 10  (June 20) — Groups E · F (MD2)
  -- ============================================================
  (33,'F','NLD','SWE','NRG Stadium',             '2026-06-20T17:00:00Z'),
  -- 12:00 CDT(-5) → 17:00 UTC
  (34,'E','DEU','CIV','BMO Field',               '2026-06-20T20:00:00Z'),
  -- 16:00 EDT(-4) → 20:00 UTC
  (35,'E','ECU','CUW','Arrowhead Stadium',       '2026-06-21T00:00:00Z'),
  -- 19:00 CDT(-5) → 00:00 UTC
  (36,'F','TUN','JPN','Estadio BBVA',            '2026-06-21T04:00:00Z'),
  -- 22:00 CDT(-6) → 04:00 UTC

  -- ============================================================
  --  MATCHDAY 11  (June 21) — Groups G · H (MD2)
  -- ============================================================
  (37,'H','ESP','SAU','Mercedes-Benz Stadium',   '2026-06-21T16:00:00Z'),
  -- 12:00 EDT(-4) → 16:00 UTC
  (38,'G','BEL','IRN','SoFi Stadium',            '2026-06-21T19:00:00Z'),
  -- 12:00 PDT(-7) → 19:00 UTC
  (39,'H','URY','CPV','Hard Rock Stadium',       '2026-06-21T22:00:00Z'),
  -- 18:00 EDT(-4) → 22:00 UTC
  (40,'G','NZL','EGY','BC Place',                '2026-06-22T01:00:00Z'),
  -- 18:00 PDT(-7) → 01:00 UTC

  -- ============================================================
  --  MATCHDAY 12  (June 22) — Groups I · J (MD2)
  -- ============================================================
  (41,'J','ARG','AUT','AT&T Stadium',            '2026-06-22T17:00:00Z'),
  -- 12:00 CDT(-5) → 17:00 UTC
  (42,'I','FRA','IRQ','Lincoln Financial Field', '2026-06-22T21:00:00Z'),
  -- 17:00 EDT(-4) → 21:00 UTC
  (43,'I','NOR','SEN','MetLife Stadium',         '2026-06-23T00:00:00Z'),
  -- 20:00 EDT(-4) → 00:00 UTC
  (44,'J','JOR','DZA','Levi''s Stadium',         '2026-06-23T03:00:00Z'),
  -- 20:00 PDT(-7) → 03:00 UTC

  -- ============================================================
  --  MATCHDAY 13  (June 23) — Groups K · L (MD2)
  -- ============================================================
  (45,'K','PRT','UZB','NRG Stadium',             '2026-06-23T17:00:00Z'),
  -- 12:00 CDT(-5) → 17:00 UTC
  (46,'L','ENG','GHA','Gillette Stadium',        '2026-06-23T20:00:00Z'),
  -- 16:00 EDT(-4) → 20:00 UTC
  (47,'L','PAN','HRV','BMO Field',               '2026-06-23T23:00:00Z'),
  -- 19:00 EDT(-4) → 23:00 UTC
  (48,'K','COL','COD','Estadio Akron',           '2026-06-24T02:00:00Z'),
  -- 20:00 CDT(-6) → 02:00 UTC

  -- ============================================================
  --  MATCHDAY 14  (June 24) — Groups B · C · A (MD3, simultaneous)
  -- ============================================================
  -- Group B: both at 12:00 PDT(-7) = 19:00 UTC
  (49,'B','CHE','CAN','BC Place',                '2026-06-24T19:00:00Z'),
  (50,'B','BIH','QAT','Lumen Field',             '2026-06-24T19:00:00Z'),
  -- Group C: both at 18:00 EDT(-4) = 22:00 UTC
  (51,'C','SCO','BRA','Hard Rock Stadium',       '2026-06-24T22:00:00Z'),
  (52,'C','MAR','HTI','Mercedes-Benz Stadium',   '2026-06-24T22:00:00Z'),
  -- Group A: both at 19:00 CDT(-6) = 01:00 UTC (Jun 25)
  (53,'A','CZE','MEX','Estadio Azteca',          '2026-06-25T01:00:00Z'),
  (54,'A','ZAF','KOR','Estadio BBVA',            '2026-06-25T01:00:00Z'),

  -- ============================================================
  --  MATCHDAY 15  (June 25) — Groups E · F · D (MD3, simultaneous)
  -- ============================================================
  -- Group E: both at 16:00 EDT(-4) = 20:00 UTC
  (55,'E','CUW','CIV','Lincoln Financial Field', '2026-06-25T20:00:00Z'),
  (56,'E','ECU','DEU','MetLife Stadium',         '2026-06-25T20:00:00Z'),
  -- Group F: both at 18:00 CDT(-5) = 23:00 UTC
  (57,'F','JPN','SWE','AT&T Stadium',            '2026-06-25T23:00:00Z'),
  (58,'F','TUN','NLD','Arrowhead Stadium',       '2026-06-25T23:00:00Z'),
  -- Group D: both at 19:00 PDT(-7) = 02:00 UTC (Jun 26)
  (59,'D','TUR','USA','SoFi Stadium',            '2026-06-26T02:00:00Z'),
  (60,'D','PRY','AUS','Levi''s Stadium',         '2026-06-26T02:00:00Z'),

  -- ============================================================
  --  MATCHDAY 16  (June 26) — Groups I · H · G (MD3, simultaneous)
  -- ============================================================
  -- Group I: both at 15:00 EDT(-4) = 19:00 UTC
  (61,'I','NOR','FRA','Gillette Stadium',        '2026-06-26T19:00:00Z'),
  (62,'I','SEN','IRQ','BMO Field',               '2026-06-26T19:00:00Z'),
  -- Group H: CPV-SAU at 19:00 CDT(-5) = 00:00 UTC; URY-ESP at 18:00 CDT(-6) = 00:00 UTC
  (63,'H','CPV','SAU','NRG Stadium',             '2026-06-27T00:00:00Z'),
  (64,'H','URY','ESP','Estadio Akron',           '2026-06-27T00:00:00Z'),
  -- Group G: both at 20:00 PDT(-7) = 03:00 UTC (Jun 27)
  (65,'G','EGY','IRN','Lumen Field',             '2026-06-27T03:00:00Z'),
  (66,'G','NZL','BEL','BC Place',                '2026-06-27T03:00:00Z'),

  -- ============================================================
  --  MATCHDAY 17  (June 27) — Groups L · K · J (MD3, simultaneous)
  -- ============================================================
  -- Group L: both at 17:00 EDT(-4) = 21:00 UTC
  (67,'L','PAN','ENG','MetLife Stadium',         '2026-06-27T21:00:00Z'),
  (68,'L','HRV','GHA','Lincoln Financial Field', '2026-06-27T21:00:00Z'),
  -- Group K: both at 19:30 EDT(-4) = 23:30 UTC
  (69,'K','COL','PRT','Hard Rock Stadium',       '2026-06-27T23:30:00Z'),
  (70,'K','COD','UZB','Mercedes-Benz Stadium',   '2026-06-27T23:30:00Z'),
  -- Group J: both at 21:00 CDT(-5) = 02:00 UTC (Jun 28)
  (71,'J','DZA','AUT','Arrowhead Stadium',       '2026-06-28T02:00:00Z'),
  (72,'J','JOR','ARG','AT&T Stadium',            '2026-06-28T02:00:00Z')

) AS v(num, grp, hiso, aiso, sname, sch)
JOIN tm ht ON ht.code_iso3 = v.hiso
JOIN tm at ON at.code_iso3 = v.aiso
JOIN sm sd ON sd.name     = v.sname;
