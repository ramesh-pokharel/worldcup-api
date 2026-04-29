-- V8__fix_teams_2026_draw.sql
-- Corrects the 48 qualified teams to match the official December 2025 FIFA draw.
--
-- WHY this migration is needed:
--   V3 was written before qualifying completed. Bosnia beat Italy in the UEFA
--   playoff (Nov 2025) and Iraq returned after a 40-year absence. The draw
--   on Dec 5, 2025 also reshuffled every group, so V3 group assignments are
--   entirely wrong.
--
-- WHAT changes:
--   13 teams removed : ITA SRB POL CMR DNK HUN SVK SVN ROU BOL PER VEN NGA
--   13 teams added   : CZE BIH SCO TUR SWE NOR TUN CPV DZA QAT HTI CUW (+ COD already exists)
--   35 teams updated : group_id and fifa_ranking corrected for all that remain
--
-- FK ordering matters:
--   players.team_id          → teams(id)  NO CASCADE → delete players first
--   matches.home/away_team_id → teams(id)  NO CASCADE → NULL those columns first
--   tournament_predictions   → teams(id)  NO CASCADE → NULL those columns first
--   user_favorite_teams.team_id → teams(id)  ON DELETE CASCADE → handled automatically
--   comments.team_id          → teams(id)  ON DELETE CASCADE → handled automatically

-- ---------------------------------------------------------------
-- STEP 1 – Insert 12 missing countries (DR Congo / COD already in V2)
-- ---------------------------------------------------------------
INSERT INTO countries (name, code_iso2, code_iso3, flag_emoji, confederation_id) VALUES
  -- UEFA (6)
  ('Czech Republic',         'CZ', 'CZE', '🇨🇿', (SELECT id FROM confederations WHERE code = 'UEFA')),
  ('Bosnia and Herzegovina', 'BA', 'BIH', '🇧🇦', (SELECT id FROM confederations WHERE code = 'UEFA')),
  ('Scotland',               'GS', 'SCO', '🏴󠁧󠁢󠁳󠁣󠁴󠁿', (SELECT id FROM confederations WHERE code = 'UEFA')),
  ('Turkey',                 'TR', 'TUR', '🇹🇷', (SELECT id FROM confederations WHERE code = 'UEFA')),
  ('Sweden',                 'SE', 'SWE', '🇸🇪', (SELECT id FROM confederations WHERE code = 'UEFA')),
  ('Norway',                 'NO', 'NOR', '🇳🇴', (SELECT id FROM confederations WHERE code = 'UEFA')),
  -- CAF (3)
  ('Tunisia',    'TN', 'TUN', '🇹🇳', (SELECT id FROM confederations WHERE code = 'CAF')),
  ('Cape Verde', 'CV', 'CPV', '🇨🇻', (SELECT id FROM confederations WHERE code = 'CAF')),
  ('Algeria',    'DZ', 'DZA', '🇩🇿', (SELECT id FROM confederations WHERE code = 'CAF')),
  -- AFC (1)
  ('Qatar', 'QA', 'QAT', '🇶🇦', (SELECT id FROM confederations WHERE code = 'AFC')),
  -- CONCACAF (2)
  ('Haiti',    'HT', 'HTI', '🇭🇹', (SELECT id FROM confederations WHERE code = 'CONCACAF')),
  ('Curaçao',  'CW', 'CUW', '🇨🇼', (SELECT id FROM confederations WHERE code = 'CONCACAF'))
ON CONFLICT (code_iso3) DO NOTHING;

-- ---------------------------------------------------------------
-- STEP 2 – Delete players of the 13 non-qualifying teams
--          players.team_id has no ON DELETE CASCADE so must go first
-- ---------------------------------------------------------------
DELETE FROM players
WHERE team_id IN (
    SELECT t.id FROM teams t
    JOIN countries c ON c.id = t.country_id
    WHERE c.code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

-- ---------------------------------------------------------------
-- STEP 3 – NULL out matches that reference the 13 removed teams
--          matches.home/away_team_id has no ON DELETE CASCADE
--          NOTE: a V9 migration will reassign all group-stage fixtures
--                to the correct teams once the full schedule is rebuilt
-- ---------------------------------------------------------------
UPDATE matches
SET home_team_id = NULL
WHERE home_team_id IN (
    SELECT t.id FROM teams t
    JOIN countries c ON c.id = t.country_id
    WHERE c.code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

UPDATE matches
SET away_team_id = NULL
WHERE away_team_id IN (
    SELECT t.id FROM teams t
    JOIN countries c ON c.id = t.country_id
    WHERE c.code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

-- ---------------------------------------------------------------
-- STEP 4 – NULL out tournament_predictions referencing removed teams
--          (no ON DELETE CASCADE on either FK column)
-- ---------------------------------------------------------------
UPDATE tournament_predictions
SET predicted_winner_id = NULL
WHERE predicted_winner_id IN (
    SELECT t.id FROM teams t
    JOIN countries c ON c.id = t.country_id
    WHERE c.code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

UPDATE tournament_predictions
SET predicted_runner_up = NULL
WHERE predicted_runner_up IN (
    SELECT t.id FROM teams t
    JOIN countries c ON c.id = t.country_id
    WHERE c.code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

-- ---------------------------------------------------------------
-- STEP 5 – Delete the 13 non-qualifying teams
--          user_favorite_teams.team_id and comments.team_id both have
--          ON DELETE CASCADE so those rows are cleaned up automatically
-- ---------------------------------------------------------------
DELETE FROM teams
WHERE country_id IN (
    SELECT id FROM countries
    WHERE code_iso3 IN (
        'BOL','PER','VEN','NGA','ITA','POL','CMR','DNK','HUN','SVK','SVN','ROU','SRB'
    )
);

-- ---------------------------------------------------------------
-- STEP 6 – Insert the 13 new qualifying teams
-- ---------------------------------------------------------------
INSERT INTO teams (country_id, fifa_ranking, group_id, manager, kit_primary, kit_secondary, is_host) VALUES
  -- Group A
  ((SELECT id FROM countries WHERE code_iso3 = 'CZE'),  38, 'A', 'Ivan Hasek',        '#D7002A', '#FFFFFF', FALSE),
  -- Group B
  ((SELECT id FROM countries WHERE code_iso3 = 'QAT'),  62, 'B', 'Marquez Lopez',     '#8D1B3D', '#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3 = 'BIH'),  58, 'B', 'Sergej Barbarez',   '#002395', '#FFCD00', FALSE),
  -- Group C
  ((SELECT id FROM countries WHERE code_iso3 = 'SCO'),  39, 'C', 'Steve Clarke',      '#003063', '#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3 = 'HTI'),  78, 'C', 'Marc Collat',       '#003087', '#EF3E42', FALSE),
  -- Group D
  ((SELECT id FROM countries WHERE code_iso3 = 'TUR'),  31, 'D', 'Vincenzo Montella', '#E30A17', '#FFFFFF', FALSE),
  -- Group E
  ((SELECT id FROM countries WHERE code_iso3 = 'CUW'),  79, 'E', 'Remko Bicentini',   '#003DA5', '#F5C518', FALSE),
  -- Group F
  ((SELECT id FROM countries WHERE code_iso3 = 'SWE'),  25, 'F', 'Jon Dahl Tomasson', '#FECC02', '#006AA7', FALSE),
  ((SELECT id FROM countries WHERE code_iso3 = 'TUN'),  35, 'F', 'Jalel Kadri',       '#E70013', '#FFFFFF', FALSE),
  -- Group H
  ((SELECT id FROM countries WHERE code_iso3 = 'CPV'),  68, 'H', 'Pedro Leitao',      '#003893', '#FFFFFF', FALSE),
  -- Group I
  ((SELECT id FROM countries WHERE code_iso3 = 'NOR'),  30, 'I', 'Stale Solbakken',   '#EF2B2D', '#FFFFFF', FALSE),
  -- Group J
  ((SELECT id FROM countries WHERE code_iso3 = 'DZA'),  33, 'J', 'Vladimir Petkovic', '#FFFFFF', '#006B35', FALSE),
  -- Group K
  ((SELECT id FROM countries WHERE code_iso3 = 'COD'),  52, 'K', 'Sebastien Desabre', '#007FFF', '#F7D000', FALSE)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- STEP 7 – Update group assignments + rankings for the 35 teams
--          that qualified but had wrong group assignments in V3.
--          Uses a VALUES join so all 35 rows are updated in one pass.
-- ---------------------------------------------------------------
UPDATE teams t
SET group_id     = v.grp,
    fifa_ranking = v.ranking
FROM (VALUES
    -- Group A
    ('MEX', 'A',  11), ('KOR', 'A',  23), ('ZAF', 'A',  60),
    -- Group B
    ('CAN', 'B',  38), ('CHE', 'B',  22),
    -- Group C
    ('BRA', 'C',   4), ('MAR', 'C',  14),
    -- Group D
    ('USA', 'D',  11), ('PRY', 'D',  72), ('AUS', 'D',  24),
    -- Group E
    ('DEU', 'E',   8), ('ECU', 'E',  40), ('CIV', 'E',  46),
    -- Group F
    ('NLD', 'F',  12), ('JPN', 'F',  18),
    -- Group G
    ('BEL', 'G',   3), ('IRN', 'G',  22), ('EGY', 'G',  42), ('NZL', 'G',  93),
    -- Group H
    ('ESP', 'H',   7), ('URY', 'H',  17), ('SAU', 'H',  54),
    -- Group I
    ('FRA', 'I',   2), ('SEN', 'I',  20), ('IRQ', 'I',  59),
    -- Group J
    ('ARG', 'J',   1), ('AUT', 'J',  26), ('JOR', 'J',  71),
    -- Group K
    ('PRT', 'K',   6), ('COL', 'K',  13), ('UZB', 'K',  65),
    -- Group L
    ('ENG', 'L',   5), ('HRV', 'L',  10), ('PAN', 'L',  58), ('GHA', 'L',  56)
) AS v(iso3, grp, ranking)
JOIN countries c ON c.code_iso3 = v.iso3
WHERE t.country_id = c.id;

-- England's manager changed: Gareth Southgate → Thomas Tuchel (Oct 2024)
UPDATE teams
SET manager = 'Thomas Tuchel'
WHERE country_id = (SELECT id FROM countries WHERE code_iso3 = 'ENG');
