-- V3__seed_teams.sql
-- 48 teams with group assignments (Groups A-L, 4 per group)
-- FIFA Rankings as of early 2026
-- FIXES vs original: Croatia was erroneously listed as 'CRO' in Group F
--   (wrong ISO3) AND 'HRV' in Group L — duplicate.
--   Corrected: HRV in Group F (correct ISO3), SVN (Slovenia) in Group L.

INSERT INTO teams (country_id, fifa_ranking, group_id, manager, kit_primary, kit_secondary, is_host) VALUES
  -- Group A
  ((SELECT id FROM countries WHERE code_iso3='USA'), 11,'A','Mauricio Pochettino','#002868','#BF0A30', TRUE),
  ((SELECT id FROM countries WHERE code_iso3='URY'), 17,'A','Marcelo Bielsa',     '#5FFFFF','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='PAN'), 58,'A','Thomas Christiansen','#FFFFFF','#CC0001', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='BOL'), 74,'A','Oscar Villegas',     '#009A44','#D52B1E', FALSE),
  -- Group B
  ((SELECT id FROM countries WHERE code_iso3='ARG'),  1,'B','Lionel Scaloni',    '#75AADB','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='CAN'), 38,'B','Jesse Marsch',      '#D80621','#FFFFFF', TRUE),
  ((SELECT id FROM countries WHERE code_iso3='PER'), 67,'B','Jorge Fossati',     '#D91023','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='NZL'), 93,'B','Darren Bazeley',    '#FFFFFF','#000000', FALSE),
  -- Group C
  ((SELECT id FROM countries WHERE code_iso3='MEX'), 16,'C','Javier Aguirre',    '#006847','#D62828', TRUE),
  ((SELECT id FROM countries WHERE code_iso3='ECU'), 40,'C','Sebastian Beccacece','#FFD100','#000066', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='VEN'), 44,'C','Fernando Batista',  '#CF142B','#00247D', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='IRQ'), 59,'C','Jesus Casas',       '#007A3D','#FFFFFF', FALSE),
  -- Group D
  ((SELECT id FROM countries WHERE code_iso3='BRA'),  4,'D','Dorival Junior',    '#009C3B','#FDD116', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='NGA'), 39,'D','Bruno Labbadia',    '#008751','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='COL'), 13,'D','Nestor Lorenzo',    '#FCD116','#003087', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='JPN'), 18,'D','Hajime Moriyasu',   '#003087','#FFFFFF', FALSE),
  -- Group E
  ((SELECT id FROM countries WHERE code_iso3='FRA'),  2,'E','Didier Deschamps',  '#002395','#ED2939', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='MAR'), 14,'E','Walid Regragui',    '#C1272D','#006233', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='AUS'), 24,'E','Tony Popovic',      '#002B7F','#FFD700', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='SEN'), 20,'E','Aliou Cisse',       '#00853F','#FDEF42', FALSE),
  -- Group F  (HRV = correct ISO3 for Croatia)
  ((SELECT id FROM countries WHERE code_iso3='ESP'),  7,'F','Luis de la Fuente', '#AA151B','#F1BF00', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='PRT'),  6,'F','Roberto Martinez',  '#006600','#FF0000', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='HRV'), 10,'F','Zlatko Dalic',      '#FF0000','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='BEL'),  3,'F','Domenico Tedesco',  '#000000','#FDDA24', FALSE),
  -- Group G
  ((SELECT id FROM countries WHERE code_iso3='ENG'),  5,'G','Gareth Southgate',  '#FFFFFF','#012169', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='DEU'),  8,'G','Julian Nagelsmann', '#FFFFFF','#000000', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='GHA'), 56,'G','Otto Addo',         '#006B3F','#FCD116', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='SRB'), 34,'G','Dragan Stojkovic',  '#0C4076','#FFFFFF', FALSE),
  -- Group H
  ((SELECT id FROM countries WHERE code_iso3='NLD'), 12,'H','Ronald Koeman',     '#FF6600','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='ITA'),  9,'H','Luciano Spalletti', '#003399','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='EGY'), 42,'H','Hossam Hassan',     '#CC1800','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='JOR'), 71,'H','Hussein Ammouta',   '#007A3D','#FFFFFF', FALSE),
  -- Group I
  ((SELECT id FROM countries WHERE code_iso3='CHE'), 22,'I','Murat Yakin',       '#FF0000','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='AUT'), 26,'I','Ralf Rangnick',     '#CC0000','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='SAU'), 54,'I','Herve Renard',      '#006C35','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='CIV'), 46,'I','Emerse Fae',        '#F77F00','#009A44', FALSE),
  -- Group J
  ((SELECT id FROM countries WHERE code_iso3='KOR'), 23,'J','Hong Myung-bo',     '#CC0001','#002395', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='IRN'), 22,'J','Amir Ghalenoei',    '#239F40','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='POL'), 31,'J','Michal Probierz',   '#FFFFFF','#DC143C', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='CMR'), 43,'J','Marc Brys',         '#007A5E','#CE1126', FALSE),
  -- Group K
  ((SELECT id FROM countries WHERE code_iso3='DNK'), 21,'K','Brian Riemer',      '#C60C30','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='HUN'), 35,'K','Marco Rossi',       '#CE2939','#477050', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='SVK'), 47,'K','Francesco Calzona', '#FFFFFF','#003DA5', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='ZAF'), 60,'K','Hugo Broos',        '#009A44','#FFB81C', FALSE),
  -- Group L  (SVN = Slovenia replaces duplicate Croatia)
  ((SELECT id FROM countries WHERE code_iso3='SVN'), 56,'L','Matjaz Kek',        '#003DA5','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='UZB'), 65,'L','Srecko Katanec',    '#1EB53A','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='PRY'), 72,'L','Daniel Garnero',    '#CC0001','#FFFFFF', FALSE),
  ((SELECT id FROM countries WHERE code_iso3='ROU'), 48,'L','Mircea Lucescu',    '#002B7F','#FFD700', FALSE)
ON CONFLICT DO NOTHING;
