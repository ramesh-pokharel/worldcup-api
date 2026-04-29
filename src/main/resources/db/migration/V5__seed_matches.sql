-- V5__seed_matches.sql  –  Full 2026 FIFA World Cup schedule (104 matches)
-- HOW:  Two CTEs resolve ISO3 → team_id and stadium_name → stadium_id.
--       Group stage matches (1-72) carry real team IDs.
--       Knockout matches (73-104) have NULL team IDs (TBD after group stage).
-- All times UTC.  MD3 games within the same group share identical kick-off times.

-- ---------------------------------------------------------------
-- GROUP STAGE  (matches 1-72)
-- ---------------------------------------------------------------
WITH
  tm AS (
    SELECT c.code_iso3, t.id AS tid
    FROM teams t
    JOIN countries c ON t.country_id = c.id
  ),
  sm AS (SELECT name, id AS sid FROM stadiums)
INSERT INTO matches (match_number, stage, group_id,
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
  -- ===== MD1: June 11–23 =====
  ( 1,'A','USA','URY','MetLife Stadium',        '2026-06-11T20:00:00Z'),
  ( 2,'A','PAN','BOL','AT&T Stadium',           '2026-06-12T20:00:00Z'),
  ( 3,'B','ARG','CAN','MetLife Stadium',        '2026-06-12T23:00:00Z'),
  ( 4,'B','PER','NZL','SoFi Stadium',           '2026-06-13T20:00:00Z'),
  ( 5,'C','MEX','ECU','Estadio Azteca',         '2026-06-13T23:00:00Z'),
  ( 6,'C','VEN','IRQ','NRG Stadium',            '2026-06-14T20:00:00Z'),
  ( 7,'D','BRA','NGA','SoFi Stadium',           '2026-06-14T23:00:00Z'),
  ( 8,'D','COL','JPN','Lincoln Financial Field','2026-06-15T20:00:00Z'),
  ( 9,'E','FRA','MAR','Lumen Field',            '2026-06-15T23:00:00Z'),
  (10,'E','AUS','SEN','Hard Rock Stadium',      '2026-06-16T20:00:00Z'),
  (11,'F','ESP','PRT','MetLife Stadium',        '2026-06-16T23:00:00Z'),
  (12,'F','HRV','BEL','AT&T Stadium',           '2026-06-17T20:00:00Z'),
  (13,'G','ENG','DEU','Arrowhead Stadium',      '2026-06-17T23:00:00Z'),
  (14,'G','GHA','SRB','Lincoln Financial Field','2026-06-18T20:00:00Z'),
  (15,'H','NLD','ITA','SoFi Stadium',           '2026-06-18T23:00:00Z'),
  (16,'H','EGY','JOR','Allegiant Stadium',      '2026-06-19T20:00:00Z'),
  (17,'I','CHE','AUT','Levi''s Stadium',        '2026-06-19T23:00:00Z'),
  (18,'I','SAU','CIV','NRG Stadium',            '2026-06-20T20:00:00Z'),
  (19,'J','KOR','IRN','BC Place',               '2026-06-20T23:00:00Z'),
  (20,'J','POL','CMR','BMO Field',              '2026-06-21T20:00:00Z'),
  (21,'K','DNK','HUN','Lumen Field',            '2026-06-21T23:00:00Z'),
  (22,'K','SVK','ZAF','Hard Rock Stadium',      '2026-06-22T20:00:00Z'),
  (23,'L','SVN','UZB','Arrowhead Stadium',      '2026-06-22T23:00:00Z'),
  (24,'L','PRY','ROU','Allegiant Stadium',      '2026-06-23T20:00:00Z'),

  -- ===== MD2: June 24 – July 6 =====
  (25,'A','USA','PAN','NRG Stadium',            '2026-06-25T20:00:00Z'),
  (26,'A','URY','BOL','Levi''s Stadium',        '2026-06-25T23:00:00Z'),
  (27,'B','ARG','PER','MetLife Stadium',        '2026-06-26T20:00:00Z'),
  (28,'B','CAN','NZL','BC Place',               '2026-06-26T23:00:00Z'),
  (29,'C','MEX','VEN','Estadio Azteca',         '2026-06-27T20:00:00Z'),
  (30,'C','ECU','IRQ','Estadio BBVA',           '2026-06-27T23:00:00Z'),
  (31,'D','BRA','COL','AT&T Stadium',           '2026-06-28T20:00:00Z'),
  (32,'D','NGA','JPN','Lincoln Financial Field','2026-06-28T23:00:00Z'),
  (33,'E','FRA','AUS','SoFi Stadium',           '2026-06-29T20:00:00Z'),
  (34,'E','MAR','SEN','Hard Rock Stadium',      '2026-06-29T23:00:00Z'),
  (35,'F','ESP','HRV','MetLife Stadium',        '2026-06-30T20:00:00Z'),
  (36,'F','PRT','BEL','Arrowhead Stadium',      '2026-06-30T23:00:00Z'),
  (37,'G','ENG','GHA','Lumen Field',            '2026-07-01T20:00:00Z'),
  (38,'G','DEU','SRB','NRG Stadium',            '2026-07-01T23:00:00Z'),
  (39,'H','NLD','EGY','AT&T Stadium',           '2026-07-02T20:00:00Z'),
  (40,'H','ITA','JOR','Allegiant Stadium',      '2026-07-02T23:00:00Z'),
  (41,'I','CHE','SAU','Levi''s Stadium',        '2026-07-03T20:00:00Z'),
  (42,'I','AUT','CIV','Hard Rock Stadium',      '2026-07-03T23:00:00Z'),
  (43,'J','KOR','POL','BC Place',               '2026-07-04T20:00:00Z'),
  (44,'J','IRN','CMR','BMO Field',              '2026-07-04T23:00:00Z'),
  (45,'K','DNK','SVK','Lincoln Financial Field','2026-07-05T20:00:00Z'),
  (46,'K','HUN','ZAF','NRG Stadium',            '2026-07-05T23:00:00Z'),
  (47,'L','SVN','PRY','MetLife Stadium',        '2026-07-06T20:00:00Z'),
  (48,'L','UZB','ROU','SoFi Stadium',           '2026-07-06T23:00:00Z'),

  -- ===== MD3: July 7–14  (pairs within same group are simultaneous) =====
  (49,'A','USA','BOL','MetLife Stadium',        '2026-07-08T20:00:00Z'),
  (50,'A','URY','PAN','AT&T Stadium',           '2026-07-08T20:00:00Z'),
  (51,'B','ARG','NZL','MetLife Stadium',        '2026-07-09T20:00:00Z'),
  (52,'B','CAN','PER','BC Place',               '2026-07-09T20:00:00Z'),
  (53,'C','MEX','IRQ','Estadio Azteca',         '2026-07-09T23:00:00Z'),
  (54,'C','ECU','VEN','Estadio Akron',          '2026-07-09T23:00:00Z'),
  (55,'D','BRA','JPN','SoFi Stadium',           '2026-07-10T20:00:00Z'),
  (56,'D','NGA','COL','Lincoln Financial Field','2026-07-10T20:00:00Z'),
  (57,'E','FRA','SEN','Lumen Field',            '2026-07-10T23:00:00Z'),
  (58,'E','MAR','AUS','Arrowhead Stadium',      '2026-07-10T23:00:00Z'),
  (59,'F','ESP','BEL','MetLife Stadium',        '2026-07-11T20:00:00Z'),
  (60,'F','PRT','HRV','NRG Stadium',            '2026-07-11T20:00:00Z'),
  (61,'G','ENG','SRB','Gillette Stadium',       '2026-07-11T23:00:00Z'),
  (62,'G','DEU','GHA','Allegiant Stadium',      '2026-07-11T23:00:00Z'),
  (63,'H','NLD','JOR','AT&T Stadium',           '2026-07-12T20:00:00Z'),
  (64,'H','ITA','EGY','Levi''s Stadium',        '2026-07-12T20:00:00Z'),
  (65,'I','CHE','CIV','SoFi Stadium',           '2026-07-12T23:00:00Z'),
  (66,'I','AUT','SAU','Hard Rock Stadium',      '2026-07-12T23:00:00Z'),
  (67,'J','KOR','CMR','BC Place',               '2026-07-13T20:00:00Z'),
  (68,'J','IRN','POL','BMO Field',              '2026-07-13T20:00:00Z'),
  (69,'K','DNK','ZAF','Lincoln Financial Field','2026-07-13T23:00:00Z'),
  (70,'K','HUN','SVK','NRG Stadium',            '2026-07-13T23:00:00Z'),
  (71,'L','SVN','ROU','MetLife Stadium',        '2026-07-14T20:00:00Z'),
  (72,'L','UZB','PRY','SoFi Stadium',           '2026-07-14T20:00:00Z')
) AS v(num, grp, hiso, aiso, sname, sch)
JOIN tm ht ON ht.code_iso3 = v.hiso
JOIN tm at ON at.code_iso3 = v.aiso
JOIN sm sd ON sd.name = v.sname;

-- ---------------------------------------------------------------
-- KNOCKOUT STAGE  (matches 73-104)
-- Teams are NULL (TBD).  Stadiums are the largest venues.
-- WHY separate INSERT: NULL team_ids can't JOIN the team CTE.
-- ---------------------------------------------------------------
WITH sm AS (SELECT name, id AS sid FROM stadiums)
INSERT INTO matches (match_number, stage, home_team_id, away_team_id,
                     stadium_id, scheduled_at, status)
SELECT
  v.num,
  v.stg::match_stage,
  NULL,
  NULL,
  sd.sid,
  v.sch::timestamptz,
  'SCHEDULED'::match_status
FROM (VALUES
  -- Round of 32 (16 matches, July 16-23)
  ( 73,'ROUND_OF_32','MetLife Stadium',        '2026-07-16T20:00:00Z'),
  ( 74,'ROUND_OF_32','AT&T Stadium',           '2026-07-16T23:00:00Z'),
  ( 75,'ROUND_OF_32','SoFi Stadium',           '2026-07-17T20:00:00Z'),
  ( 76,'ROUND_OF_32','Arrowhead Stadium',      '2026-07-17T23:00:00Z'),
  ( 77,'ROUND_OF_32','Lincoln Financial Field','2026-07-18T20:00:00Z'),
  ( 78,'ROUND_OF_32','Lumen Field',            '2026-07-18T23:00:00Z'),
  ( 79,'ROUND_OF_32','NRG Stadium',            '2026-07-19T20:00:00Z'),
  ( 80,'ROUND_OF_32','Allegiant Stadium',      '2026-07-19T23:00:00Z'),
  ( 81,'ROUND_OF_32','Hard Rock Stadium',      '2026-07-20T20:00:00Z'),
  ( 82,'ROUND_OF_32','Gillette Stadium',       '2026-07-20T23:00:00Z'),
  ( 83,'ROUND_OF_32','BC Place',               '2026-07-21T20:00:00Z'),
  ( 84,'ROUND_OF_32','BMO Field',              '2026-07-21T23:00:00Z'),
  ( 85,'ROUND_OF_32','Estadio Azteca',         '2026-07-22T20:00:00Z'),
  ( 86,'ROUND_OF_32','Estadio BBVA',           '2026-07-22T23:00:00Z'),
  ( 87,'ROUND_OF_32','Estadio Akron',          '2026-07-23T20:00:00Z'),
  ( 88,'ROUND_OF_32','Levi''s Stadium',        '2026-07-23T23:00:00Z'),
  -- Round of 16 (8 matches, July 25-28)
  ( 89,'ROUND_OF_16','MetLife Stadium',        '2026-07-25T20:00:00Z'),
  ( 90,'ROUND_OF_16','AT&T Stadium',           '2026-07-25T23:00:00Z'),
  ( 91,'ROUND_OF_16','SoFi Stadium',           '2026-07-26T20:00:00Z'),
  ( 92,'ROUND_OF_16','Arrowhead Stadium',      '2026-07-26T23:00:00Z'),
  ( 93,'ROUND_OF_16','MetLife Stadium',        '2026-07-27T20:00:00Z'),
  ( 94,'ROUND_OF_16','SoFi Stadium',           '2026-07-27T23:00:00Z'),
  ( 95,'ROUND_OF_16','AT&T Stadium',           '2026-07-28T20:00:00Z'),
  ( 96,'ROUND_OF_16','Arrowhead Stadium',      '2026-07-28T23:00:00Z'),
  -- Quarter-finals (4 matches, July 30-31)
  ( 97,'QUARTER_FINAL','MetLife Stadium',      '2026-07-30T20:00:00Z'),
  ( 98,'QUARTER_FINAL','AT&T Stadium',         '2026-07-30T23:00:00Z'),
  ( 99,'QUARTER_FINAL','MetLife Stadium',      '2026-07-31T20:00:00Z'),
  (100,'QUARTER_FINAL','SoFi Stadium',         '2026-07-31T23:00:00Z'),
  -- Semi-finals (2 matches, Aug 3-4)
  (101,'SEMI_FINAL','MetLife Stadium',         '2026-08-03T20:00:00Z'),
  (102,'SEMI_FINAL','AT&T Stadium',            '2026-08-04T20:00:00Z'),
  -- Third place (Aug 7)
  (103,'THIRD_PLACE','AT&T Stadium',           '2026-08-07T20:00:00Z'),
  -- Final (Aug 8)
  (104,'FINAL','MetLife Stadium',              '2026-08-08T20:00:00Z')
) AS v(num, stg, sname, sch)
JOIN sm sd ON sd.name = v.sname;
