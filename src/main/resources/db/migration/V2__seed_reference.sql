-- V2__seed_reference.sql  –  Confederations, Countries & Stadiums
-- WHY confederations first: countries FK references confederations(id),
--     so parent rows must exist before children.

-- ---------------------------------------------------------------
-- CONFEDERATIONS
-- ---------------------------------------------------------------
INSERT INTO confederations (code, name) VALUES
  ('UEFA',     'Union of European Football Associations'),
  ('CONMEBOL', 'South American Football Confederation'),
  ('CAF',      'Confederation of African Football'),
  ('AFC',      'Asian Football Confederation'),
  ('CONCACAF', 'Confederation of North, Central America and Caribbean Association Football'),
  ('OFC',      'Oceania Football Confederation')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- COUNTRIES  (50 qualified nations + hosts)
-- Added Bolivia, Peru, Slovenia vs original file
-- ---------------------------------------------------------------
INSERT INTO countries (name, code_iso2, code_iso3, flag_emoji, confederation_id) VALUES
-- UEFA (18)
  ('Germany',     'DE','DEU','🇩🇪',(SELECT id FROM confederations WHERE code='UEFA')),
  ('France',      'FR','FRA','🇫🇷',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Spain',       'ES','ESP','🇪🇸',(SELECT id FROM confederations WHERE code='UEFA')),
  ('England',     'GB','ENG','🏴󠁧󠁢󠁥󠁮󠁧󠁿',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Portugal',    'PT','PRT','🇵🇹',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Netherlands', 'NL','NLD','🇳🇱',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Italy',       'IT','ITA','🇮🇹',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Belgium',     'BE','BEL','🇧🇪',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Croatia',     'HR','HRV','🇭🇷',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Austria',     'AT','AUT','🇦🇹',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Switzerland', 'CH','CHE','🇨🇭',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Denmark',     'DK','DNK','🇩🇰',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Poland',      'PL','POL','🇵🇱',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Hungary',     'HU','HUN','🇭🇺',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Serbia',      'RS','SRB','🇷🇸',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Slovakia',    'SK','SVK','🇸🇰',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Slovenia',    'SI','SVN','🇸🇮',(SELECT id FROM confederations WHERE code='UEFA')),
  ('Romania',     'RO','ROU','🇷🇴',(SELECT id FROM confederations WHERE code='UEFA')),
-- CONMEBOL (8)
  ('Argentina',   'AR','ARG','🇦🇷',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Brazil',      'BR','BRA','🇧🇷',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Colombia',    'CO','COL','🇨🇴',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Uruguay',     'UY','URY','🇺🇾',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Ecuador',     'EC','ECU','🇪🇨',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Venezuela',   'VE','VEN','🇻🇪',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Paraguay',    'PY','PRY','🇵🇾',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Peru',        'PE','PER','🇵🇪',(SELECT id FROM confederations WHERE code='CONMEBOL')),
  ('Bolivia',     'BO','BOL','🇧🇴',(SELECT id FROM confederations WHERE code='CONMEBOL')),
-- CAF (9)
  ('Morocco',      'MA','MAR','🇲🇦',(SELECT id FROM confederations WHERE code='CAF')),
  ('Senegal',      'SN','SEN','🇸🇳',(SELECT id FROM confederations WHERE code='CAF')),
  ('Egypt',        'EG','EGY','🇪🇬',(SELECT id FROM confederations WHERE code='CAF')),
  ('Nigeria',      'NG','NGA','🇳🇬',(SELECT id FROM confederations WHERE code='CAF')),
  ('South Africa', 'ZA','ZAF','🇿🇦',(SELECT id FROM confederations WHERE code='CAF')),
  ('DR Congo',     'CD','COD','🇨🇩',(SELECT id FROM confederations WHERE code='CAF')),
  ('Ivory Coast',  'CI','CIV','🇨🇮',(SELECT id FROM confederations WHERE code='CAF')),
  ('Cameroon',     'CM','CMR','🇨🇲',(SELECT id FROM confederations WHERE code='CAF')),
  ('Ghana',        'GH','GHA','🇬🇭',(SELECT id FROM confederations WHERE code='CAF')),
-- AFC (8)
  ('Japan',        'JP','JPN','🇯🇵',(SELECT id FROM confederations WHERE code='AFC')),
  ('South Korea',  'KR','KOR','🇰🇷',(SELECT id FROM confederations WHERE code='AFC')),
  ('Iran',         'IR','IRN','🇮🇷',(SELECT id FROM confederations WHERE code='AFC')),
  ('Australia',    'AU','AUS','🇦🇺',(SELECT id FROM confederations WHERE code='AFC')),
  ('Saudi Arabia', 'SA','SAU','🇸🇦',(SELECT id FROM confederations WHERE code='AFC')),
  ('Iraq',         'IQ','IRQ','🇮🇶',(SELECT id FROM confederations WHERE code='AFC')),
  ('Jordan',       'JO','JOR','🇯🇴',(SELECT id FROM confederations WHERE code='AFC')),
  ('Uzbekistan',   'UZ','UZB','🇺🇿',(SELECT id FROM confederations WHERE code='AFC')),
-- CONCACAF (6 – 3 hosts)
  ('United States','US','USA','🇺🇸',(SELECT id FROM confederations WHERE code='CONCACAF')),
  ('Canada',       'CA','CAN','🇨🇦',(SELECT id FROM confederations WHERE code='CONCACAF')),
  ('Mexico',       'MX','MEX','🇲🇽',(SELECT id FROM confederations WHERE code='CONCACAF')),
  ('Panama',       'PA','PAN','🇵🇦',(SELECT id FROM confederations WHERE code='CONCACAF')),
  ('Honduras',     'HN','HND','🇭🇳',(SELECT id FROM confederations WHERE code='CONCACAF')),
  ('Costa Rica',   'CR','CRI','🇨🇷',(SELECT id FROM confederations WHERE code='CONCACAF')),
-- OFC (1)
  ('New Zealand',  'NZ','NZL','🇳🇿',(SELECT id FROM confederations WHERE code='OFC'))
ON CONFLICT (code_iso3) DO NOTHING;

-- ---------------------------------------------------------------
-- STADIUMS  (16 official 2026 World Cup venues)
-- ---------------------------------------------------------------
INSERT INTO stadiums (name, city, country, capacity, latitude, longitude) VALUES
  ('MetLife Stadium',         'East Rutherford, NJ', 'USA',    82500, 40.8135, -74.0745),
  ('SoFi Stadium',            'Inglewood, CA',        'USA',    70240, 33.9534,-118.3392),
  ('AT&T Stadium',            'Arlington, TX',         'USA',    80000, 32.7479, -97.0929),
  ('Levi''s Stadium',         'Santa Clara, CA',       'USA',    68500, 37.4032,-121.9699),
  ('Lincoln Financial Field', 'Philadelphia, PA',      'USA',    69796, 39.9008, -75.1675),
  ('Arrowhead Stadium',       'Kansas City, MO',       'USA',    76416, 39.0490, -94.4839),
  ('Allegiant Stadium',       'Las Vegas, NV',         'USA',    65000, 36.0909,-115.1833),
  ('Lumen Field',             'Seattle, WA',           'USA',    68740, 47.5952,-122.3316),
  ('NRG Stadium',             'Houston, TX',           'USA',    72220, 29.6847, -95.4107),
  ('Hard Rock Stadium',       'Miami, FL',             'USA',    65326, 25.9580, -80.2389),
  ('Gillette Stadium',        'Foxborough, MA',        'USA',    65878, 42.0909, -71.2643),
  ('BMO Field',               'Toronto, ON',           'Canada', 45736, 43.6333, -79.4186),
  ('BC Place',                'Vancouver, BC',         'Canada', 54500, 49.2767,-123.1118),
  ('Estadio Azteca',          'Mexico City',           'Mexico', 87523, 19.3029, -99.1503),
  ('Estadio BBVA',            'Monterrey',             'Mexico', 53500, 25.6694,-100.2440),
  ('Estadio Akron',           'Guadalajara',           'Mexico', 49850, 20.6867,-103.4671);
