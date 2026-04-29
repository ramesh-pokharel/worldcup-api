-- V4__seed_players.sql  –  Squad data for all 48 qualified nations
-- HOW: One CTE maps ISO3 code → team_id (runs once, not per row).
--      All player rows live in a VALUES table and JOIN to that CTE.
--      This avoids repeating the subquery 700+ times.
-- Positions allowed: GK CB LB RB LWB RWB CDM CM CAM LM RM LW RW SS ST CF

WITH team_ids AS (
  SELECT c.code_iso3, t.id AS team_id
  FROM teams t
  JOIN countries c ON t.country_id = c.id
)
INSERT INTO players (team_id, name, date_of_birth, nationality, position,
                     shirt_number, club, caps, goals, height_cm, market_value_eur, is_captain)
SELECT
  ti.team_id,
  v.name,
  v.dob::date,
  v.nationality,
  v.pos,
  v.num::smallint,
  v.club,
  v.caps::int,
  v.goals::int,
  v.ht::smallint,
  v.mkt::bigint,
  v.cap::boolean
FROM team_ids ti
JOIN (VALUES

  -- =========================================================
  -- GROUP A
  -- =========================================================

  -- United States (USA)
  ('USA','Matt Turner',          '1994-06-24','United States','GK', 1,'Crystal Palace',      55, 0,191, 8000000,false),
  ('USA','Zack Steffen',         '1995-04-02','United States','GK',18,'Colorado Rapids',      32, 0,193, 2000000,false),
  ('USA','Sergino Dest',         '2000-11-03','United States','RB', 2,'PSV Eindhoven',        52, 4,174,18000000,false),
  ('USA','Miles Robinson',       '1997-05-22','United States','CB', 4,'FC Cincinnati',        40, 2,190, 8000000,false),
  ('USA','Mark McKenzie',        '1999-10-05','United States','CB', 5,'Toulouse',             30, 1,188, 7000000,false),
  ('USA','Antonee Robinson',     '1997-08-08','United States','LB', 3,'Fulham',               55, 3,178,20000000,false),
  ('USA','Tyler Adams',          '1999-02-14','United States','CDM', 4,'Bournemouth',         58, 5,178,20000000,false),
  ('USA','Weston McKennie',      '1998-08-28','United States','CM', 8,'Juventus',             60,10,185,22000000,false),
  ('USA','Yunus Musah',          '2002-11-29','United States','CM',10,'AC Milan',             40, 4,180,30000000,false),
  ('USA','Christian Pulisic',    '1998-09-18','United States','CAM',10,'AC Milan',            75,30,177,35000000,true),
  ('USA','Tim Weah',             '2000-02-22','United States','RW',21,'Juventus',             40, 8,183,18000000,false),
  ('USA','Josh Sargent',         '2000-02-20','United States','ST', 9,'Norwich City',         40,10,185,10000000,false),
  ('USA','Giovanni Reyna',       '2002-11-13','United States','CAM',19,'Borussia Dortmund',   28, 6,185,25000000,false),

  -- Uruguay (URY)
  ('URY','Sergio Rochet',        '1993-03-23','Uruguay','GK', 1,'Nacional',               45, 0,188, 3000000,false),
  ('URY','Nahitan Nandez',       '1996-12-28','Uruguay','RB', 4,'Cagliari',               55, 5,181, 8000000,false),
  ('URY','Diego Godin',          '1986-02-16','Uruguay','CB', 2,'Velez Sarsfield',        160, 9,189, 1000000,false),
  ('URY','Sebastian Coates',     '1991-10-07','Uruguay','CB', 3,'Sporting CP',             55, 5,195, 5000000,false),
  ('URY','Mathias Olivera',      '1997-10-31','Uruguay','LB', 16,'Napoli',                40, 2,180,22000000,false),
  ('URY','Rodrigo Bentancur',    '1997-06-25','Uruguay','CM', 5,'Tottenham',              65,12,187,35000000,false),
  ('URY','Federico Valverde',    '1998-07-22','Uruguay','CM', 8,'Real Madrid',            60,15,182,90000000,false),
  ('URY','Giorgian de Arrascaeta','1994-06-01','Uruguay','CAM', 10,'Flamengo',            65,20,177,15000000,false),
  ('URY','Facundo Pellistri',    '2001-12-20','Uruguay','RW', 21,'Manchester United',     35, 5,172,20000000,false),
  ('URY','Darwin Nunez',         '1999-06-24','Uruguay','ST', 9,'Liverpool',              50,25,187,80000000,false),
  ('URY','Luis Suarez',          '1987-01-24','Uruguay','CF', 9,'River Plate',            142,68,182, 3000000,true),
  ('URY','Facundo Torres',       '2001-05-12','Uruguay','LW', 11,'Orlando City',          20, 6,172,18000000,false),
  ('URY','Maxi Gomez',           '1996-08-14','Uruguay','ST', 22,'Trabzonspor',           40,10,188, 4000000,false),

  -- Panama (PAN)
  ('PAN','Luis Mejia',           '1997-01-24','Panama','GK', 1,'Independiente',           40, 0,185, 1500000,false),
  ('PAN','Eric Davis',           '1984-12-30','Panama','CB', 3,'Basel',                   130, 5,187, 1000000,false),
  ('PAN','Fidel Escobar',        '1993-01-23','Panama','CB', 4,'New York City FC',         70, 3,190, 2000000,false),
  ('PAN','Harold Cummings',      '1992-05-09','Panama','CB', 5,'San Jose Earthquakes',    60, 4,186, 1500000,false),
  ('PAN','Roderick Miller',      '1997-07-23','Panama','LB', 14,'Club Tijuana',           45, 1,183, 1000000,false),
  ('PAN','Adalberto Carrasquilla','1998-10-16','Panama','CM', 8,'Herediano',              50, 8,177, 4000000,false),
  ('PAN','Edgar Barcenas',       '1991-08-11','Panama','LW', 11,'Salernitana',            75,12,175, 2000000,false),
  ('PAN','Alberto Quintero',     '1987-05-01','Panama','LW', 7,'Herediano',               90,18,170, 1000000,false),
  ('PAN','Rolando Blackburn',    '1997-06-06','Panama','RW', 17,'Philadelphia Union',     40, 8,183, 2000000,false),
  ('PAN','Gabriel Torres',       '1988-06-15','Panama','CF', 9,'Atletico Nacional',       90,30,173, 1500000,false),
  ('PAN','Cecilio Waterman',     '1993-01-02','Panama','ST', 20,'Como',                   55,15,177, 3000000,false),
  ('PAN','Jose Fajardo',         '1998-12-25','Panama','ST', 19,'Atlanta United',         38, 9,182, 3000000,false),

  -- Bolivia (BOL)
  ('BOL','Carlos Lampe',         '1988-08-13','Bolivia','GK', 1,'Bolivar',                90, 0,185, 1000000,false),
  ('BOL','Luis Haquin',          '1994-01-27','Bolivia','CB', 2,'Bolivar',                60, 2,186, 1200000,false),
  ('BOL','Diego Bejarano',       '1988-03-04','Bolivia','CB', 3,'Always Ready',           70, 4,180,  800000,false),
  ('BOL','Jairo Quinteros',      '1992-09-08','Bolivia','LB', 6,'Bolivar',               60, 1,177,  800000,false),
  ('BOL','Erwin Saavedra',       '1994-04-27','Bolivia','RB', 2,'Always Ready',           45, 2,175,  700000,false),
  ('BOL','Moises Paniagua',      '1995-06-11','Bolivia','CDM', 5,'The Strongest',         50, 3,179,  600000,false),
  ('BOL','Leonel Justiniano',    '2000-11-01','Bolivia','CM', 8,'Nacional Potosi',         25, 3,180,  900000,false),
  ('BOL','Henry Vaca',           '1993-11-01','Bolivia','LW', 11,'Bolivar',               65, 8,172,  700000,false),
  ('BOL','Marc Enoumba',         '1989-04-22','Bolivia','CM', 10,'Bolivar',               40, 5,180,  600000,false),
  ('BOL','Marcelo Moreno',       '1987-06-18','Bolivia','ST', 9,'Fluminense',             100,31,186, 1500000,true),
  ('BOL','Bruno Miranda',        '1993-03-14','Bolivia','ST', 23,'Atletico San Luis',     50,14,177, 1000000,false),

  -- =========================================================
  -- GROUP B
  -- =========================================================

  -- Argentina (ARG)
  ('ARG','Emiliano Martinez',    '1992-09-02','Argentina','GK', 23,'Aston Villa',         50, 0,195,18000000,false),
  ('ARG','Gonzalo Montiel',      '1997-01-01','Argentina','RB', 19,'Nottingham Forest',   32, 1,178,15000000,false),
  ('ARG','Cristian Romero',      '1998-04-27','Argentina','CB', 13,'Tottenham',           42, 1,185,60000000,false),
  ('ARG','Lisandro Martinez',    '1998-01-18','Argentina','CB', 25,'Manchester United',   40, 1,182,55000000,false),
  ('ARG','Nicolas Tagliafico',   '1992-08-31','Argentina','LB', 3,'Lyon',                 62, 4,172, 7000000,false),
  ('ARG','Rodrigo De Paul',      '1994-05-24','Argentina','CM', 7,'Atletico Madrid',      85,20,183,25000000,false),
  ('ARG','Enzo Fernandez',       '2001-01-17','Argentina','CM', 24,'Chelsea',             38, 8,180,80000000,false),
  ('ARG','Alexis Mac Allister',  '2001-12-24','Argentina','CM', 20,'Liverpool',           35, 7,177,75000000,false),
  ('ARG','Lionel Messi',         '1987-06-24','Argentina','CAM',10,'Inter Miami',         187,109,170,70000000,true),
  ('ARG','Angel Di Maria',       '1988-02-14','Argentina','RW', 11,'Benfica',             145,31,178, 8000000,false),
  ('ARG','Julian Alvarez',       '2000-01-31','Argentina','ST', 9,'Atletico Madrid',      45,24,170,80000000,false),
  ('ARG','Lautaro Martinez',     '1997-08-22','Argentina','CF', 22,'Inter Milan',         65,30,174,90000000,false),
  ('ARG','Paulo Dybala',         '1993-11-15','Argentina','CAM',21,'Roma',                35,29,177,25000000,false),
  ('ARG','Leandro Paredes',      '1994-06-29','Argentina','CDM', 5,'Roma',                55, 8,180,12000000,false),
  ('ARG','Nahuel Molina',        '1998-04-06','Argentina','RB', 26,'Atletico Madrid',     48, 9,179,40000000,false),

  -- Canada (CAN)
  ('CAN','Maxime Crepeau',       '1994-05-11','Canada','GK', 1,'Los Angeles FC',          45, 0,188, 3000000,false),
  ('CAN','Richie Laryea',        '1995-01-07','Canada','RB', 22,'Nottingham Forest',      50, 4,174, 8000000,false),
  ('CAN','Kamal Miller',         '1997-09-27','Canada','CB', 4,'Portland Timbers',        45, 3,188, 5000000,false),
  ('CAN','Derek Cornelius',      '1996-08-26','Canada','CB', 3,'Panathinaikos',           40, 2,186, 4000000,false),
  ('CAN','Alphonso Davies',      '2000-11-02','Canada','LB', 11,'Bayern Munich',          65,15,182,80000000,false),
  ('CAN','Atiba Hutchinson',     '1983-02-08','Canada','CM', 8,'Besiktas',               110, 9,183, 1000000,false),
  ('CAN','Stephen Eustaquio',    '1996-12-21','Canada','CM', 7,'Porto',                   55, 8,178,20000000,false),
  ('CAN','Jonathan Osorio',      '1992-06-20','Canada','CM', 10,'Toronto FC',              75,12,174, 5000000,false),
  ('CAN','Tajon Buchanan',       '1999-02-08','Canada','RW', 21,'Inter Milan',            50,10,181,20000000,false),
  ('CAN','Jonathan David',       '2000-01-14','Canada','ST', 9,'Lille',                   55,32,180,60000000,true),
  ('CAN','Cyle Larin',           '1995-04-17','Canada','ST', 17,'Valladolid',             75,28,187,10000000,false),
  ('CAN','Lucas Cavallini',      '1992-11-28','Canada','CF', 20,'Vancouver Whitecaps',    52,20,190, 3000000,false),
  ('CAN','Ismaeel Mohammed',     '2003-03-23','Canada','LW', 19,'Eintracht Frankfurt',    18, 3,176,12000000,false),

  -- Peru (PER)
  ('PER','Pedro Gallese',        '1990-01-11','Peru','GK', 1,'Orlando City',              85, 0,186, 3000000,false),
  ('PER','Luis Advincula',       '1990-03-02','Peru','RB', 17,'Boca Juniors',             90, 5,175, 3000000,false),
  ('PER','Alexander Callens',    '1992-05-04','Peru','CB', 6,'AEK Athens',               55, 3,181, 2000000,false),
  ('PER','Carlos Zambrano',      '1989-07-10','Peru','CB', 4,'Boca Juniors',              80, 4,185, 1500000,false),
  ('PER','Miguel Trauco',        '1992-08-25','Peru','LB', 3,'San Jose Earthquakes',      75, 3,169, 2000000,false),
  ('PER','Renato Tapia',         '1995-07-28','Peru','CDM', 5,'Leganes',                  70, 5,183,12000000,false),
  ('PER','Yoshimar Yotun',       '1990-04-07','Peru','CM', 8,'Cruz Azul',                 90,10,180, 2000000,false),
  ('PER','Christian Cueva',      '1991-11-23','Peru','CAM',10,'Alianza Lima',             95,28,170, 3000000,true),
  ('PER','Andre Carrillo',       '1991-06-14','Peru','RW', 11,'Al-Qadsiah',               80,20,180, 3000000,false),
  ('PER','Gianluca Lapadula',    '1990-02-07','Peru','ST', 9,'Cagliari',                  50,20,177, 4000000,false),
  ('PER','Paolo Guerrero',       '1984-01-01','Peru','CF', 9,'LDU Quito',                103,40,183, 1000000,false),
  ('PER','Edison Flores',        '1994-05-14','Peru','CAM',20,'San Jose Earthquakes',     60,13,167, 2000000,false),

  -- New Zealand (NZL)
  ('NZL','Stefan Marinovic',     '1991-01-10','New Zealand','GK', 1,'Vancouver Whitecaps', 55, 0,196, 1000000,false),
  ('NZL','Liberato Cacace',      '2000-09-09','New Zealand','LB', 3,'Empoli',              35, 2,176, 5000000,false),
  ('NZL','Michael Boxall',       '1988-07-05','New Zealand','CB', 4,'Minnesota United',    70, 3,190, 1500000,false),
  ('NZL','Bill Tuiloma',         '1995-01-28','New Zealand','CB', 5,'Portland Timbers',    50, 2,190, 2000000,false),
  ('NZL','Nando Pijnaker',       '1996-04-11','New Zealand','CB', 6,'Almeria',             35, 1,193, 2000000,false),
  ('NZL','Clayton Lewis',        '1997-02-23','New Zealand','CM', 8,'Austin FC',           55, 4,178, 2000000,false),
  ('NZL','Joe Bell',             '2000-06-15','New Zealand','CDM', 5,'Nashville SC',       30, 2,181, 3000000,false),
  ('NZL','Elijah Just',          '1999-09-14','New Zealand','CM', 10,'Brann',              28, 4,183, 2000000,false),
  ('NZL','Sarpreet Singh',       '1999-09-18','New Zealand','LW', 11,'Jahn Regensburg',   30, 5,172, 3000000,false),
  ('NZL','Chris Wood',           '1991-12-07','New Zealand','ST', 9,'Nottingham Forest',   90,29,190,12000000,true),
  ('NZL','Ben Waine',            '2001-01-13','New Zealand','ST', 20,'Vikingur Reykjavik', 25, 7,187, 2000000,false),
  ('NZL','Matthew Garbett',      '2000-01-15','New Zealand','CM', 15,'Troyes',             20, 2,183, 2000000,false),

  -- =========================================================
  -- GROUP C
  -- =========================================================

  -- Mexico (MEX)
  ('MEX','Guillermo Ochoa',      '1985-07-13','Mexico','GK', 1,'Club America',           140, 0,183, 3000000,false),
  ('MEX','Jorge Sanchez',        '1998-04-04','Mexico','RB', 22,'Ajax',                   45, 2,177,10000000,false),
  ('MEX','Cesar Montes',         '1997-04-24','Mexico','CB', 3,'Espanyol',               45, 1,188, 8000000,false),
  ('MEX','Johan Vasquez',        '1999-12-04','Mexico','CB', 4,'Genoa',                   35, 1,190, 8000000,false),
  ('MEX','Jesus Gallardo',       '1994-08-15','Mexico','LB', 23,'Monterrey',              70, 4,173, 6000000,false),
  ('MEX','Edson Alvarez',        '1997-10-24','Mexico','CDM', 6,'West Ham',              65, 5,185,25000000,false),
  ('MEX','Hector Herrera',       '1990-04-19','Mexico','CM', 16,'Houston Dynamo',        115,18,174, 3000000,false),
  ('MEX','Orbelin Pineda',       '1996-03-24','Mexico','CAM',21,'AEK Athens',            60,16,168, 6000000,false),
  ('MEX','Hirving Lozano',       '1995-07-30','Mexico','RW', 22,'San Diego FC',          80,29,174,12000000,false),
  ('MEX','Raul Jimenez',         '1991-05-05','Mexico','ST', 9,'Fulham',                 100,35,187,12000000,true),
  ('MEX','Alexis Vega',          '1997-02-24','Mexico','LW', 11,'Toluca',                50,14,177, 6000000,false),
  ('MEX','Roberto Alvarado',     '1999-01-07','Mexico','CAM', 10,'Guadalajara',          40,10,173, 6000000,false),
  ('MEX','Uriel Antuna',         '1997-08-21','Mexico','RW', 17,'Cruz Azul',             45,10,177, 4000000,false),

  -- Ecuador (ECU)
  ('ECU','Alexander Dominguez',  '1987-06-05','Ecuador','GK', 1,'LDU Quito',             85, 0,186, 1500000,false),
  ('ECU','Angelo Preciado',      '1998-07-08','Ecuador','RB', 22,'Genk',                 45, 2,175, 8000000,false),
  ('ECU','Piero Hincapie',       '2002-01-09','Ecuador','CB', 3,'Bayer Leverkusen',      40, 1,182,40000000,false),
  ('ECU','Robert Arboleda',      '1991-10-22','Ecuador','CB', 4,'Sao Paulo',             65, 5,188, 4000000,false),
  ('ECU','Pervis Estupinan',     '1998-01-21','Ecuador','LB', 17,'Brighton',             55, 5,172,30000000,false),
  ('ECU','Moises Caicedo',       '2001-11-02','Ecuador','CDM', 8,'Chelsea',              45, 4,180,90000000,false),
  ('ECU','Carlos Gruezo',        '1995-04-19','Ecuador','CM', 5,'Augsburg',              70, 4,170, 5000000,false),
  ('ECU','Jeremy Sarmiento',     '2002-06-16','Ecuador','LW', 11,'Brighton',             30, 5,178,15000000,false),
  ('ECU','Gonzalo Plata',        '2000-11-01','Ecuador','RW', 10,'Valladolid',           35, 8,175,10000000,false),
  ('ECU','Enner Valencia',       '1989-11-04','Ecuador','CF', 13,'Internacional',        85,40,177, 5000000,true),
  ('ECU','Michael Estrada',      '1996-04-08','Ecuador','ST', 9,'Cruzeiro',              45,12,187, 6000000,false),
  ('ECU','Jose Cifuentes',       '1999-03-12','Ecuador','CM', 6,'Los Angeles FC',        35, 4,180,12000000,false),

  -- Venezuela (VEN)
  ('VEN','Wuilker Farinez',      '1998-07-15','Venezuela','GK', 1,'Millonarios',         60, 0,187, 4000000,false),
  ('VEN','Rolf Feltscher',       '1990-10-15','Venezuela','RB', 2,'Al-Qadsiah',          75, 2,184, 2000000,false),
  ('VEN','Yordan Osorio',        '1998-03-25','Venezuela','CB', 4,'Tigres',              45, 1,186, 5000000,false),
  ('VEN','Nahuel Ferraresi',     '1999-08-16','Venezuela','CB', 3,'Columbus Crew',       35, 2,186, 8000000,false),
  ('VEN','Miguel Navarro',       '2001-01-26','Venezuela','LB', 23,'Villarreal',         30, 1,176, 8000000,false),
  ('VEN','Yangel Herrera',       '1998-01-07','Venezuela','CM', 10,'Girona',             65,18,186,30000000,true),
  ('VEN','Tomas Rincon',         '1988-01-16','Venezuela','CDM', 8,'Parma',              120,10,178, 1500000,false),
  ('VEN','Josef Martinez',       '1993-05-19','Venezuela','ST', 9,'Inter Miami',         80,32,172, 5000000,false),
  ('VEN','Salomon Rondon',       '1989-09-16','Venezuela','CF', 9,'São Paulo',           115,37,186, 2000000,false),
  ('VEN','Darwin Machis',        '1993-02-07','Venezuela','LW', 11,'Granada',            80,20,170, 3000000,false),
  ('VEN','Jan Arango',           '1997-08-14','Venezuela','RW', 17,'Boavista',           35, 8,177, 5000000,false),

  -- Iraq (IRQ)
  ('IRQ','Jalal Hassan',         '1987-06-12','Iraq','GK', 1,'Al-Shorta',               80, 0,185, 1000000,false),
  ('IRQ','Ali Adnan',            '1993-12-19','Iraq','LB', 3,'Amed SK',                 95, 6,178, 2000000,false),
  ('IRQ','Mustafa Nadhim',       '1997-08-07','Iraq','CB', 4,'Al-Shorta',              50, 2,186, 1200000,false),
  ('IRQ','Ahmed Ibrahim',        '1994-11-15','Iraq','CB', 5,'Duhok',                  55, 3,184,  800000,false),
  ('IRQ','Rebin Sulaka',         '1993-08-31','Iraq','RB', 2,'Ashdod',                 45, 1,180, 1000000,false),
  ('IRQ','Amjed Attwan',         '1996-10-21','Iraq','CDM', 6,'Al-Quwa Al-Jawiya',     50, 4,177,  800000,false),
  ('IRQ','Hussein Ali',          '1997-04-11','Iraq','CM', 8,'Al-Shorta',              45, 6,176,  900000,false),
  ('IRQ','Aiham Ousou',          '1999-04-25','Iraq','CM', 10,'Duhok',                 30, 5,178, 1000000,false),
  ('IRQ','Mohanad Ali',          '1995-07-08','Iraq','LW', 11,'Al-Quwa Al-Jawiya',     55,14,177,  800000,false),
  ('IRQ','Aymen Hussein',        '1997-01-23','Iraq','ST', 9,'Al-Shorta',              50,18,184, 1500000,true),
  ('IRQ','Karrar Mohammed',      '2001-03-17','Iraq','RW', 17,'Al-Zawra',              25, 5,175, 1200000,false),

  -- =========================================================
  -- GROUP D
  -- =========================================================

  -- Brazil (BRA)
  ('BRA','Alisson',              '1992-10-02','Brazil','GK', 1,'Liverpool',             90, 0,193,30000000,false),
  ('BRA','Danilo',               '1991-07-15','Brazil','RB', 2,'Juventus',             100, 9,183, 8000000,false),
  ('BRA','Eder Militao',         '1998-01-18','Brazil','CB', 3,'Real Madrid',           60, 4,186,60000000,false),
  ('BRA','Marquinhos',           '1994-05-14','Brazil','CB', 4,'PSG',                  105, 9,183,35000000,false),
  ('BRA','Guilherme Arana',      '1997-04-14','Brazil','LB', 6,'Atletico Mineiro',     55, 4,174,18000000,false),
  ('BRA','Casemiro',             '1992-02-23','Brazil','CDM', 5,'Manchester United',   90,17,185,18000000,false),
  ('BRA','Bruno Guimaraes',      '1997-11-16','Brazil','CM', 18,'Newcastle United',    55,12,182,80000000,false),
  ('BRA','Lucas Paqueta',        '1997-08-27','Brazil','CAM',10,'West Ham',            70,18,180,55000000,false),
  ('BRA','Rodrygo',              '2001-01-09','Brazil','RW', 11,'Real Madrid',         55,20,174,80000000,false),
  ('BRA','Vinicius Jr',          '2000-07-12','Brazil','LW', 17,'Real Madrid',         70,31,176,200000000,false),
  ('BRA','Raphinha',             '1996-12-14','Brazil','RW', 19,'Barcelona',           65,25,176,80000000,false),
  ('BRA','Endrick',              '2006-07-21','Brazil','ST', 9,'Real Madrid',          20, 8,173,80000000,false),
  ('BRA','Richarlison',          '1997-05-10','Brazil','ST', 9,'Tottenham',            60,22,184,35000000,false),
  ('BRA','Gabriel Magalhaes',    '1997-12-19','Brazil','CB', 5,'Arsenal',              40, 3,191,50000000,false),
  ('BRA','Matheus Cunha',        '1999-06-27','Brazil','CF', 9,'Wolves',               30,10,180,35000000,false),

  -- Nigeria (NGA)
  ('NGA','Francis Uzoho',        '1998-10-28','Nigeria','GK', 1,'Omonia',              35, 0,196, 3000000,false),
  ('NGA','Ola Aina',             '1996-10-08','Nigeria','RB', 2,'Nottingham Forest',   55, 3,183,12000000,false),
  ('NGA','William Troost-Ekong', '1994-09-01','Nigeria','CB', 5,'PAOK',               80, 4,190, 5000000,false),
  ('NGA','Calvin Bassey',        '2000-01-23','Nigeria','CB', 3,'Fulham',              35, 1,186,22000000,false),
  ('NGA','Zaidu Sanusi',         '1998-03-11','Nigeria','LB', 23,'Porto',              40, 2,183, 8000000,false),
  ('NGA','Alex Iwobi',           '1996-05-03','Nigeria','CM', 17,'Fulham',             80,10,181,18000000,false),
  ('NGA','Frank Onyeka',         '1998-01-01','Nigeria','CDM', 8,'Brentford',          40, 2,183,10000000,false),
  ('NGA','Samuel Chukwueze',     '1999-05-22','Nigeria','RW', 11,'AC Milan',           50,13,171,25000000,false),
  ('NGA','Ademola Lookman',      '1997-10-20','Nigeria','LW', 10,'Atalanta',           45,18,175,40000000,false),
  ('NGA','Victor Osimhen',       '1998-12-29','Nigeria','ST', 9,'Galatasaray',         55,38,185,80000000,true),
  ('NGA','Taiwo Awoniyi',        '1997-08-12','Nigeria','ST', 20,'Nottingham Forest',  35,12,183,20000000,false),
  ('NGA','Kelechi Iheanacho',    '1996-10-03','Nigeria','CF', 21,'Sevilla',            60,22,183, 8000000,false),

  -- Colombia (COL)
  ('COL','Camilo Vargas',        '1992-03-04','Colombia','GK', 1,'Atlas',              55, 0,188, 3000000,false),
  ('COL','Daniel Munoz',         '1996-07-20','Colombia','RB', 2,'Crystal Palace',    45, 4,180,18000000,false),
  ('COL','Davinson Sanchez',     '1996-06-12','Colombia','CB', 6,'Galatasaray',        75, 4,188,15000000,false),
  ('COL','Yerry Mina',           '1994-09-23','Colombia','CB', 13,'Fiorentina',        55, 9,198, 6000000,false),
  ('COL','Johan Mojica',         '1992-08-21','Colombia','LB', 3,'Girona',             55, 3,181, 6000000,false),
  ('COL','Wilmar Barrios',       '1993-09-16','Colombia','CDM', 5,'Zenit',             75, 4,174, 4000000,false),
  ('COL','Matheus Uribe',        '1991-03-21','Colombia','CM', 8,'Porto',              70,10,176, 6000000,false),
  ('COL','Luis Diaz',            '1997-01-13','Colombia','LW', 11,'Liverpool',         55,24,180,80000000,false),
  ('COL','James Rodriguez',      '1991-06-12','Colombia','CAM',10,'Rayo Vallecano',    105,43,180,10000000,true),
  ('COL','Rafael Santos Borre',  '1995-11-11','Colombia','ST', 9,'Eintracht Frankfurt',70,23,181,12000000,false),
  ('COL','Jhon Duran',           '2003-12-13','Colombia','ST', 19,'Aston Villa',       25,12,184,40000000,false),
  ('COL','Richard Rios',         '2000-03-08','Colombia','CM', 20,'Palmeiras',         25, 4,181,15000000,false),

  -- Japan (JPN)
  ('JPN','Shuichi Gonda',        '1989-03-03','Japan','GK', 1,'PSV Eindhoven',        90, 0,182, 4000000,false),
  ('JPN','Hiroki Sakai',         '1990-04-14','Japan','RB', 5,'Gamba Osaka',          100, 4,177, 2000000,false),
  ('JPN','Ko Itakura',           '1997-01-27','Japan','CB', 4,'Borussia M''gladbach', 40, 2,185,12000000,false),
  ('JPN','Shogo Taniguchi',      '1991-07-10','Japan','CB', 3,'Kawasaki Frontale',    55, 5,179, 2000000,false),
  ('JPN','Yuto Nagatomo',        '1986-09-12','Japan','LB', 5,'FC Tokyo',            140,10,170, 1000000,false),
  ('JPN','Wataru Endo',          '1993-02-09','Japan','CDM', 6,'Liverpool',           65, 5,176,15000000,false),
  ('JPN','Hidemasa Morita',      '1994-07-16','Japan','CM', 10,'Sporting CP',         50,10,177,10000000,false),
  ('JPN','Junya Ito',            '1993-03-09','Japan','RW', 21,'Reims',               75,19,176,12000000,false),
  ('JPN','Ao Tanaka',            '1998-09-10','Japan','CM', 8,'Fortuna Dusseldorf',   40, 6,178,10000000,false),
  ('JPN','Daichi Kamada',        '1996-08-05','Japan','CAM',9,'Crystal Palace',       60,15,182,20000000,false),
  ('JPN','Ritsu Doan',           '1998-06-16','Japan','LW', 11,'Freiburg',            65,22,172,18000000,false),
  ('JPN','Kaoru Mitoma',         '1997-05-20','Japan','LW', 10,'Brighton',            55,18,178,35000000,false),
  ('JPN','Takumi Minamino',      '1995-01-16','Japan','ST', 9,'Monaco',               75,21,173,15000000,true),

  -- =========================================================
  -- GROUP E
  -- =========================================================

  -- France (FRA)
  ('FRA','Mike Maignan',         '1995-07-03','France','GK', 16,'AC Milan',            55, 0,191,40000000,false),
  ('FRA','Benjamin Pavard',      '1996-03-28','France','CB', 5,'Inter Milan',          65, 6,186,30000000,false),
  ('FRA','Ibrahima Konate',      '1999-05-25','France','CB', 4,'Liverpool',            40, 1,194,55000000,false),
  ('FRA','William Saliba',       '2001-03-24','France','CB', 17,'Arsenal',             40, 1,192,70000000,false),
  ('FRA','Theo Hernandez',       '1997-10-06','France','LB', 22,'AC Milan',            55,12,182,60000000,false),
  ('FRA','Aurelien Tchouameni',  '2000-01-27','France','CDM', 8,'Real Madrid',         45, 5,188,80000000,false),
  ('FRA','Adrien Rabiot',        '1995-04-03','France','CM', 14,'Juventus',            50, 7,188,20000000,false),
  ('FRA','Eduardo Camavinga',    '2002-11-10','France','CM', 18,'Real Madrid',         40, 4,181,80000000,false),
  ('FRA','Antoine Griezmann',    '1991-03-21','France','CAM', 7,'Atletico Madrid',    130,44,176,25000000,false),
  ('FRA','Ousmane Dembele',      '1997-05-15','France','RW', 11,'PSG',                65,18,178,60000000,false),
  ('FRA','Kylian Mbappe',        '1998-12-20','France','ST', 10,'Real Madrid',        100,48,182,180000000,true),
  ('FRA','Marcus Thuram',        '1997-08-06','France','CF', 9,'Inter Milan',          45,14,191,60000000,false),
  ('FRA','Randal Kolo Muani',    '1998-08-05','France','ST', 23,'Juventus',            30, 8,188,45000000,false),
  ('FRA','Dayot Upamecano',      '1998-10-27','France','CB', 4,'Bayern Munich',        50, 2,186,45000000,false),

  -- Morocco (MAR)
  ('MAR','Yassine Bounou',       '1991-04-05','Morocco','GK', 1,'Al Hilal',            75, 0,192, 8000000,false),
  ('MAR','Achraf Hakimi',        '1998-11-04','Morocco','RB', 2,'PSG',                 80,14,181,70000000,false),
  ('MAR','Nayef Aguerd',         '1996-03-30','Morocco','CB', 5,'West Ham',            50, 3,189,18000000,false),
  ('MAR','Romain Saiss',         '1990-03-26','Morocco','CB', 13,'Besiktas',           85, 8,189, 4000000,false),
  ('MAR','Noussair Mazraoui',    '1997-11-14','Morocco','LB', 3,'Manchester United',   55, 4,181,25000000,false),
  ('MAR','Sofyan Amrabat',       '1996-08-21','Morocco','CDM', 4,'Fiorentina',         60, 4,183,20000000,false),
  ('MAR','Azzedine Ounahi',      '2000-08-19','Morocco','CM', 8,'Marseille',           40, 5,183,22000000,false),
  ('MAR','Hakim Ziyech',         '1993-03-19','Morocco','CAM',11,'Galatasaray',        70,18,181,12000000,false),
  ('MAR','Sofiane Boufal',       '1993-09-17','Morocco','LW', 7,'Al-Qadsiah',          70,15,170, 5000000,false),
  ('MAR','Youssef En-Nesyri',    '1997-06-01','Morocco','ST', 9,'Fenerbahce',          60,26,192,25000000,false),
  ('MAR','Abdessamad Ezzalzouli','2001-11-15','Morocco','RW', 10,'Osasuna',            30, 8,178,20000000,false),
  ('MAR','Bilal El Khannouss',   '2004-05-10','Morocco','CAM',20,'Genk',              20, 5,179,25000000,false),
  ('MAR','Ilias Chair',          '1997-10-30','Morocco','CM', 17,'Stoke City',         30, 6,172, 8000000,true),

  -- Australia (AUS)
  ('AUS','Mathew Ryan',          '1992-04-08','Australia','GK', 1,'Real Sociedad',     85, 0,184, 4000000,false),
  ('AUS','Nathaniel Atkinson',   '1999-06-13','Australia','RB', 2,'Hearts',            35, 2,176, 3000000,false),
  ('AUS','Harry Souttar',        '1998-10-22','Australia','CB', 6,'Leicester City',    40, 5,201,10000000,false),
  ('AUS','Kye Rowles',           '1998-11-24','Australia','CB', 5,'Hearts',            30, 2,189, 4000000,false),
  ('AUS','Aziz Behich',          '1990-12-16','Australia','LB', 3,'Dundee United',     65, 3,173, 1500000,false),
  ('AUS','Jackson Irvine',       '1993-03-07','Australia','CM', 8,'St Pauli',          80,12,188, 5000000,false),
  ('AUS','Riley McGree',         '1998-11-02','Australia','CM', 18,'Middlesbrough',    40, 8,182, 8000000,false),
  ('AUS','Aaron Mooy',           '1990-09-15','Australia','CM', 13,'Celtic',           95,12,176, 3000000,false),
  ('AUS','Mathew Leckie',        '1991-02-04','Australia','RW', 7,'Melbourne City',    90,19,181, 2000000,true),
  ('AUS','Mitchell Duke',        '1991-01-18','Australia','ST', 9,'Fagiano Okayama',   50,13,185, 1500000,false),
  ('AUS','Martin Boyle',         '1993-04-25','Australia','LW', 11,'Al-Faisaly',       55,13,172, 3000000,false),
  ('AUS','Craig Goodwin',        '1992-12-16','Australia','LW', 19,'Adelaide United',  50, 8,180, 1500000,false),

  -- Senegal (SEN)
  ('SEN','Edouard Mendy',        '1992-03-01','Senegal','GK', 1,'Al Ahli',            60, 0,197,10000000,false),
  ('SEN','Bouna Sarr',           '1992-01-31','Senegal','RB', 2,'Bayern Munich',       40, 2,184, 3000000,false),
  ('SEN','Kalidou Koulibaly',    '1991-06-20','Senegal','CB', 3,'Al Hilal',            90, 7,187,15000000,false),
  ('SEN','Abdou Diallo',         '1996-05-04','Senegal','CB', 4,'RB Leipzig',          50, 4,182,12000000,false),
  ('SEN','Ismail Jakobs',        '2000-08-16','Senegal','LB', 18,'Monaco',             30, 3,181,18000000,false),
  ('SEN','Idrissa Gueye',        '1989-09-26','Senegal','CDM', 5,'Everton',            85,10,174, 5000000,false),
  ('SEN','Cheikhou Kouyate',     '1989-12-21','Senegal','CM', 8,'Nottingham Forest',   90, 7,190, 4000000,false),
  ('SEN','Pape Gueye',           '1999-01-24','Senegal','CM', 15,'Marseille',          25, 3,188, 8000000,false),
  ('SEN','Ismaila Sarr',         '1998-02-25','Senegal','RW', 17,'Marseille',          75,22,186,22000000,false),
  ('SEN','Sadio Mane',           '1992-04-10','Senegal','LW', 10,'Al Nassr',          100,34,175,15000000,true),
  ('SEN','Boulaye Dia',          '1996-11-16','Senegal','ST', 9,'Lazio',              45,15,182,20000000,false),
  ('SEN','Nicolas Jackson',      '2001-06-20','Senegal','CF', 11,'Chelsea',           25,12,185,35000000,false),

  -- =========================================================
  -- GROUP F
  -- =========================================================

  -- Spain (ESP)
  ('ESP','Unai Simon',           '1997-06-11','Spain','GK', 1,'Athletic Bilbao',       55, 0,189,25000000,false),
  ('ESP','Daniel Carvajal',      '1992-01-11','Spain','RB', 2,'Real Madrid',           95,10,173,15000000,false),
  ('ESP','Aymeric Laporte',      '1994-05-27','Spain','CB', 14,'Al Nassr',             60, 4,189,15000000,false),
  ('ESP','Robin Le Normand',     '1996-11-11','Spain','CB', 4,'Atletico Madrid',       30, 1,187,30000000,false),
  ('ESP','Marc Cucurella',       '1998-07-22','Spain','LB', 3,'Chelsea',               45, 2,172,30000000,false),
  ('ESP','Rodri',                '1996-06-22','Spain','CDM', 16,'Manchester City',     60, 3,191,120000000,false),
  ('ESP','Pedri',                '2002-11-25','Spain','CM', 8,'Barcelona',             45,11,174,100000000,false),
  ('ESP','Fabian Ruiz',          '1996-08-03','Spain','CM', 18,'PSG',                  55, 8,185,40000000,false),
  ('ESP','Lamine Yamal',         '2007-07-13','Spain','RW', 19,'Barcelona',            28,10,180,120000000,false),
  ('ESP','Nico Williams',        '2002-07-12','Spain','LW', 17,'Athletic Bilbao',      35,12,181,80000000,false),
  ('ESP','Alvaro Morata',        '1992-10-23','Spain','ST', 9,'AC Milan',              85,35,187,15000000,true),
  ('ESP','Dani Olmo',            '1998-05-07','Spain','CAM',10,'Barcelona',            50,16,180,60000000,false),
  ('ESP','Mikel Merino',         '1996-06-22','Spain','CM', 22,'Arsenal',              45, 8,189,35000000,false),

  -- Portugal (PRT)
  ('PRT','Diogo Costa',          '1999-09-19','Portugal','GK', 1,'Porto',              45, 0,189,25000000,false),
  ('PRT','Joao Cancelo',         '1994-05-27','Portugal','RB', 2,'Barcelona',          80,10,182,30000000,false),
  ('PRT','Ruben Dias',           '1997-05-14','Portugal','CB', 4,'Manchester City',    65, 5,187,65000000,false),
  ('PRT','Pepe',                 '1983-02-26','Portugal','CB', 3,'Porto',              145, 7,188, 2000000,false),
  ('PRT','Nuno Mendes',          '2002-06-19','Portugal','LB', 19,'PSG',               40, 4,177,60000000,false),
  ('PRT','Bruno Fernandes',      '1994-09-08','Portugal','CAM', 8,'Manchester United', 85,40,179,60000000,false),
  ('PRT','Bernardo Silva',       '1994-08-10','Portugal','CM', 10,'Manchester City',   90,30,173,80000000,false),
  ('PRT','Vitinha',              '2000-02-13','Portugal','CM', 16,'PSG',               35, 5,170,50000000,false),
  ('PRT','Joao Felix',           '1999-11-10','Portugal','CAM',11,'Chelsea',           65,21,181,55000000,false),
  ('PRT','Rafael Leao',          '1999-06-10','Portugal','LW', 17,'AC Milan',          55,22,188,80000000,false),
  ('PRT','Cristiano Ronaldo',    '1985-02-05','Portugal','ST', 7,'Al Nassr',           215,135,187,20000000,true),
  ('PRT','Goncalo Ramos',        '2001-06-20','Portugal','CF', 9,'PSG',               30,15,187,60000000,false),
  ('PRT','Diogo Jota',           '1996-12-04','Portugal','LW', 20,'Liverpool',         60,23,178,55000000,false),

  -- Croatia (HRV)
  ('HRV','Dominik Livakovic',    '1995-01-09','Croatia','GK', 1,'Fenerbahce',          60, 0,188,12000000,false),
  ('HRV','Josip Juranovic',      '1995-08-16','Croatia','RB', 2,'Celtic',             60, 4,173, 8000000,false),
  ('HRV','Josko Gvardiol',       '2002-01-23','Croatia','CB', 5,'Manchester City',     40, 4,185,70000000,false),
  ('HRV','Duje Caleta-Car',      '1996-09-17','Croatia','CB', 6,'Southampton',         40, 2,192,10000000,false),
  ('HRV','Borna Sosa',           '1998-01-21','Croatia','LB', 3,'Stuttgart',           45, 4,183,12000000,false),
  ('HRV','Luka Modric',          '1985-09-09','Croatia','CM', 10,'Al Qadsiah',         175,24,172, 5000000,true),
  ('HRV','Marcelo Brozovic',     '1992-11-16','Croatia','CDM', 11,'Al Nassr',          100,11,181, 8000000,false),
  ('HRV','Mateo Kovacic',        '1994-05-06','Croatia','CM', 8,'Manchester City',     95,18,177,30000000,false),
  ('HRV','Ivan Perisic',         '1989-02-02','Croatia','LW', 4,'Hajduk Split',        130,33,188, 3000000,false),
  ('HRV','Andrej Kramaric',      '1991-06-19','Croatia','ST', 9,'Hoffenheim',          90,34,177,10000000,false),
  ('HRV','Bruno Petkovic',       '1992-09-16','Croatia','CF', 22,'Dinamo Zagreb',      50,16,190, 4000000,false),
  ('HRV','Nikola Vlasic',        '1997-10-04','Croatia','CAM',13,'Torino',             55,12,179,18000000,false),
  ('HRV','Mario Pasalic',        '1995-02-09','Croatia','CM', 15,'Atalanta',           65,16,187,18000000,false),

  -- Belgium (BEL)
  ('BEL','Thibaut Courtois',     '1992-05-11','Belgium','GK', 1,'Real Madrid',         100, 0,199,25000000,false),
  ('BEL','Timothy Castagne',     '1995-12-05','Belgium','RB', 22,'Fulham',             65, 6,183,15000000,false),
  ('BEL','Wout Faes',            '1998-04-03','Belgium','CB', 4,'Leicester City',      30, 2,186,15000000,false),
  ('BEL','Jan Vertonghen',       '1987-04-24','Belgium','CB', 5,'Anderlecht',          155, 9,189, 2000000,false),
  ('BEL','Amadou Onana',         '2001-08-16','Belgium','CDM', 8,'Aston Villa',        30, 4,194,40000000,false),
  ('BEL','Kevin De Bruyne',      '1991-06-28','Belgium','CAM',7,'Manchester City',     110,30,181,60000000,true),
  ('BEL','Youri Tielemans',      '1997-05-07','Belgium','CM', 10,'Aston Villa',        70,12,176,30000000,false),
  ('BEL','Leandro Trossard',     '1994-12-04','Belgium','LW', 11,'Arsenal',            55,20,172,30000000,false),
  ('BEL','Dodi Lukebakio',       '1997-09-24','Belgium','RW', 9,'Sevilla',             40,12,181,15000000,false),
  ('BEL','Romelu Lukaku',        '1993-05-13','Belgium','ST', 9,'Napoli',              115,77,191,18000000,false),
  ('BEL','Jeremy Doku',          '2002-05-12','Belgium','LW', 14,'Manchester City',    35,10,172,60000000,false),
  ('BEL','Charles De Ketelaere', '2001-03-10','Belgium','CAM',17,'Atalanta',           40,12,192,40000000,false),

  -- =========================================================
  -- GROUP G
  -- =========================================================

  -- England (ENG)
  ('ENG','Jordan Pickford',      '1994-03-07','England','GK', 1,'Everton',             75, 0,185,20000000,false),
  ('ENG','Trent Alexander-Arnold','1998-10-07','England','RB', 66,'Real Madrid',       75,20,175,70000000,false),
  ('ENG','Harry Maguire',        '1993-03-05','England','CB', 6,'Manchester United',   70, 7,194,20000000,false),
  ('ENG','John Stones',          '1994-05-28','England','CB', 5,'Manchester City',     75, 5,188,25000000,false),
  ('ENG','Luke Shaw',            '1995-07-12','England','LB', 23,'Manchester United',  45, 2,180,25000000,false),
  ('ENG','Declan Rice',          '1999-01-14','England','CDM', 4,'Arsenal',            60, 8,185,100000000,false),
  ('ENG','Jude Bellingham',      '2003-06-29','England','CM', 10,'Real Madrid',        55,22,186,180000000,false),
  ('ENG','Phil Foden',           '2000-05-28','England','CAM',47,'Manchester City',    55,22,171,150000000,false),
  ('ENG','Bukayo Saka',          '2001-09-05','England','RW', 7,'Arsenal',             60,30,178,150000000,false),
  ('ENG','Harry Kane',           '1993-07-28','England','ST', 9,'Bayern Munich',       95,65,188,100000000,true),
  ('ENG','Marcus Rashford',      '1997-10-31','England','LW', 10,'Manchester United',  65,22,180,55000000,false),
  ('ENG','Jack Grealish',        '1995-09-10','England','LW', 11,'Manchester City',    45, 9,180,45000000,false),
  ('ENG','Kieran Trippier',      '1990-09-19','England','RB', 2,'Newcastle United',    55, 8,178,15000000,false),
  ('ENG','Raheem Sterling',      '1994-12-08','England','LW', 10,'Arsenal',            82,22,170,20000000,false),

  -- Germany (DEU)
  ('DEU','Manuel Neuer',         '1986-03-27','Germany','GK', 1,'Bayern Munich',       125, 0,193,10000000,false),
  ('DEU','Joshua Kimmich',       '1995-02-08','Germany','RB', 6,'Bayern Munich',       95,14,177,60000000,false),
  ('DEU','Antonio Rudiger',      '1993-03-03','Germany','CB', 2,'Real Madrid',         75, 4,190,25000000,false),
  ('DEU','Nico Schlotterbeck',   '1999-12-01','Germany','CB', 4,'Borussia Dortmund',   35, 2,192,30000000,false),
  ('DEU','David Raum',           '1998-04-22','Germany','LB', 3,'RB Leipzig',          35, 4,180,30000000,false),
  ('DEU','Toni Kroos',           '1990-01-04','Germany','CM', 8,'Real Madrid',         115,17,183,12000000,false),
  ('DEU','Ilkay Gundogan',       '1990-10-24','Germany','CM', 21,'Barcelona',          80,17,180,10000000,true),
  ('DEU','Jamal Musiala',        '2003-02-26','Germany','CAM',10,'Bayern Munich',      40,16,180,120000000,false),
  ('DEU','Leroy Sane',           '1996-01-11','Germany','LW', 19,'Bayern Munich',      80,22,183,40000000,false),
  ('DEU','Kai Havertz',          '1999-06-11','Germany','CF', 7,'Arsenal',             65,24,189,60000000,false),
  ('DEU','Florian Wirtz',        '2003-05-03','Germany','CAM',17,'Bayer Leverkusen',   35,15,180,150000000,false),
  ('DEU','Thomas Muller',        '1989-09-13','Germany','SS', 25,'Bayern Munich',      130,44,185, 8000000,false),
  ('DEU','Serge Gnabry',         '1995-07-14','Germany','RW', 10,'Bayern Munich',      45,21,175,30000000,false),

  -- Ghana (GHA)
  ('GHA','Lawrence Ati-Zigi',    '1997-06-18','Ghana','GK', 1,'St Gallen',            45, 0,190, 3000000,false),
  ('GHA','Andy Yiadom',          '1992-12-02','Ghana','RB', 2,'Reading',              65, 4,177, 2000000,false),
  ('GHA','Alexander Djiku',      '1994-08-09','Ghana','CB', 3,'Fenerbahce',           45, 2,184, 6000000,false),
  ('GHA','Daniel Amartey',       '1994-12-21','Ghana','CB', 5,'Leicester City',       55, 3,184, 5000000,false),
  ('GHA','Gideon Mensah',        '1998-07-21','Ghana','LB', 4,'Red Bull Salzburg',    35, 1,176, 6000000,false),
  ('GHA','Thomas Partey',        '1993-06-13','Ghana','CDM', 5,'Arsenal',             65, 9,185,25000000,true),
  ('GHA','Elisha Owusu',         '1997-08-07','Ghana','CM', 8,'Gent',                 35, 3,185, 4000000,false),
  ('GHA','Mohammed Kudus',       '2000-08-02','Ghana','CAM',10,'West Ham',            50,20,178,40000000,false),
  ('GHA','Jordan Ayew',          '1991-09-11','Ghana','LW', 11,'Crystal Palace',      90,20,182, 5000000,false),
  ('GHA','Andre Ayew',           '1989-12-17','Ghana','CAM', 10,'Le Havre',           115,23,175, 3000000,false),
  ('GHA','Antoine Semenyo',      '2000-01-08','Ghana','RW', 19,'Bournemouth',         25, 7,179,18000000,false),
  ('GHA','Osman Bukari',         '1999-12-19','Ghana','LW', 17,'Red Star Belgrade',   25, 6,183, 6000000,false),

  -- Serbia (SRB)
  ('SRB','Vanja Milinkovic-Savic','1997-02-20','Serbia','GK', 12,'Torino',            40, 0,202, 6000000,false),
  ('SRB','Strahinja Pavlovic',   '2001-05-24','Serbia','CB', 5,'Monaco',              25, 1,194,22000000,false),
  ('SRB','Nikola Milenkovic',    '1997-10-12','Serbia','CB', 4,'Nottingham Forest',   55, 5,193,18000000,false),
  ('SRB','Milos Veljkovic',      '1995-09-26','Serbia','CB', 6,'Werder Bremen',       45, 3,188, 8000000,false),
  ('SRB','Filip Mladenovic',     '1991-08-15','Serbia','LB', 3,'Lechia Gdansk',       60, 4,178, 2000000,false),
  ('SRB','Nemanja Maksimovic',   '1995-01-26','Serbia','CDM', 8,'Getafe',             55, 3,184, 6000000,false),
  ('SRB','Sergej Milinkovic-Savic','1995-02-27','Serbia','CM', 11,'Al Hilal',          75,18,191,30000000,false),
  ('SRB','Filip Kostic',         '1992-11-01','Serbia','LM', 10,'Juventus',           75,15,184,12000000,false),
  ('SRB','Andrija Zivkovic',     '1996-07-11','Serbia','RW', 17,'PAOK',               45, 8,172, 5000000,false),
  ('SRB','Aleksandar Mitrovic',  '1994-09-16','Serbia','ST', 9,'Al Hilal',            85,60,192,25000000,true),
  ('SRB','Dusan Vlahovic',       '2000-01-28','Serbia','ST', 23,'Juventus',           55,32,190,80000000,false),
  ('SRB','Luka Jovic',           '1997-12-23','Serbia','CF', 18,'AC Milan',           55,17,182,15000000,false),

  -- =========================================================
  -- GROUP H
  -- =========================================================

  -- Netherlands (NLD)
  ('NLD','Bart Verbruggen',      '2002-08-18','Netherlands','GK', 1,'Brighton',        25, 0,190,20000000,false),
  ('NLD','Denzel Dumfries',      '1996-04-18','Netherlands','RB', 22,'Inter Milan',    65,10,188,30000000,false),
  ('NLD','Virgil van Dijk',      '1991-07-08','Netherlands','CB', 4,'Liverpool',       70, 5,193,25000000,true),
  ('NLD','Stefan de Vrij',       '1992-02-05','Netherlands','CB', 6,'Inter Milan',     65, 5,189,10000000,false),
  ('NLD','Nathan Ake',           '1995-02-18','Netherlands','LB', 5,'Manchester City', 55, 6,180,30000000,false),
  ('NLD','Frenkie de Jong',      '1997-05-12','Netherlands','CM', 21,'Barcelona',      70,12,180,70000000,false),
  ('NLD','Tijjani Reijnders',    '1998-07-29','Netherlands','CM', 14,'AC Milan',       35, 6,182,40000000,false),
  ('NLD','Xavi Simons',          '2003-04-21','Netherlands','CAM',7,'RB Leipzig',      35,10,176,80000000,false),
  ('NLD','Steven Bergwijn',      '1997-10-08','Netherlands','RW', 11,'Ajax',           60,15,176,10000000,false),
  ('NLD','Cody Gakpo',           '1999-05-07','Netherlands','LW', 10,'Liverpool',      55,22,189,60000000,false),
  ('NLD','Memphis Depay',        '1994-02-13','Netherlands','ST', 9,'Corinthians',     85,44,176, 8000000,false),
  ('NLD','Donyell Malen',        '1999-01-19','Netherlands','RW', 17,'Borussia Dortmund',50,16,178,35000000,false),
  ('NLD','Mats Wieffer',         '1999-10-22','Netherlands','CDM', 6,'Brighton',       20, 2,185,25000000,false),

  -- Italy (ITA)
  ('ITA','Gianluigi Donnarumma', '1999-02-25','Italy','GK', 1,'PSG',                  65, 0,196,50000000,false),
  ('ITA','Giovanni Di Lorenzo',  '1993-08-04','Italy','RB', 2,'Napoli',               70, 8,183,18000000,false),
  ('ITA','Alessandro Bastoni',   '1999-04-08','Italy','CB', 23,'Inter Milan',          60, 4,191,60000000,false),
  ('ITA','Giorgio Scalvini',     '2003-12-11','Italy','CB', 13,'Atalanta',             20, 1,194,50000000,false),
  ('ITA','Federico Dimarco',     '1997-11-10','Italy','LB', 3,'Inter Milan',           50,12,170,40000000,false),
  ('ITA','Nicolo Barella',       '1997-02-07','Italy','CM', 18,'Inter Milan',          70,14,172,80000000,false),
  ('ITA','Lorenzo Pellegrini',   '1996-06-19','Italy','CAM',7,'Roma',                  65,18,185,25000000,false),
  ('ITA','Sandro Tonali',        '2000-05-08','Italy','CDM', 8,'Newcastle United',     30, 4,180,55000000,false),
  ('ITA','Federico Chiesa',      '1997-10-25','Italy','RW', 14,'Liverpool',            60,22,175,35000000,false),
  ('ITA','Gianluca Scamacca',    '1999-01-01','Italy','ST', 9,'Atalanta',              35,15,195,35000000,false),
  ('ITA','Matteo Retegui',       '2001-06-05','Italy','CF', 19,'Atalanta',             25,12,183,30000000,false),
  ('ITA','Giacomo Raspadori',    '2000-02-18','Italy','ST', 10,'Napoli',              35,11,168,35000000,true),
  ('ITA','Davide Frattesi',      '1999-09-22','Italy','CM', 16,'Inter Milan',          30, 6,177,35000000,false),

  -- Egypt (EGY)
  ('EGY','Mohamed El-Shenawy',   '1988-07-01','Egypt','GK', 1,'Al Ahly',              75, 0,189, 2000000,false),
  ('EGY','Ahmed Hegazi',         '1991-01-25','Egypt','CB', 5,'Al Ittihad',           75, 8,193, 3000000,false),
  ('EGY','Mohamed Abdelmonem',   '1997-11-27','Egypt','CB', 3,'Benfica',              40, 2,185, 6000000,false),
  ('EGY','Omar Kamal',           '2000-01-29','Egypt','LB', 23,'Pyramids FC',         25, 1,181, 1000000,false),
  ('EGY','Ahmed Fateh',          '1996-09-12','Egypt','RB', 2,'Zamalek',              40, 2,178, 1000000,false),
  ('EGY','Tarek Hamed',          '1988-10-14','Egypt','CDM', 6,'Zamalek',             75, 4,181, 1500000,false),
  ('EGY','Mohamed Elneny',       '1992-07-11','Egypt','CM', 4,'Arsenal',              100, 8,178, 5000000,false),
  ('EGY','Omar Marmoush',        '1999-02-07','Egypt','ST', 11,'Manchester City',     40,20,178,60000000,false),
  ('EGY','Mohamed Salah',        '1992-06-15','Egypt','RW', 10,'Liverpool',           105,58,175,40000000,true),
  ('EGY','Mostafa Mohamed',      '1998-11-26','Egypt','CF', 9,'Galatasaray',          45,16,190,10000000,false),
  ('EGY','Trezeguet',            '1994-10-01','Egypt','LW', 17,'Trabzonspor',         70,18,183, 4000000,false),

  -- Jordan (JOR)
  ('JOR','Yazeed Abo Layla',     '1990-09-09','Jordan','GK', 1,'Al-Wahdat',           75, 0,190, 1000000,false),
  ('JOR','Baha Faisal',          '1995-01-08','Jordan','RB', 2,'Al-Wahdat',           55, 2,177,  800000,false),
  ('JOR','Abdallah Nasib',       '1994-03-22','Jordan','CB', 4,'Shabab Al-Ahli',      50, 3,184,  900000,false),
  ('JOR','Yazan Al-Naimat',      '1997-06-18','Jordan','CB', 5,'Al-Wahdat',           45, 2,186,  800000,false),
  ('JOR','Ehsan Haddad',         '1992-02-11','Jordan','LB', 3,'Al-Faisaly',          60, 2,175,  700000,false),
  ('JOR','Nizar Al-Rashid',      '1993-08-23','Jordan','CDM', 6,'Al-Wehdat',          55, 3,180,  700000,false),
  ('JOR','Musa Al-Tamari',       '1997-06-06','Jordan','CAM',10,'Montpellier',        55,15,173, 3000000,false),
  ('JOR','Oday Dabbagh',         '1998-11-28','Jordan','ST', 9,'Deportivo Alaves',    35,10,185, 4000000,true),
  ('JOR','Mohammad Abu Zema',    '1999-03-14','Jordan','LW', 11,'Al-Wahdat',          30, 7,177,  800000,false),
  ('JOR','Hamza Al-Dardour',     '1995-08-15','Jordan','ST', 7,'Al-Faisaly',          50,14,183,  900000,false),
  ('JOR','Ahmad Saleh',          '2000-05-11','Jordan','CM', 17,'Al-Wahdat',          20, 2,179,  600000,false),

  -- =========================================================
  -- GROUP I
  -- =========================================================

  -- Switzerland (CHE)
  ('CHE','Yann Sommer',          '1988-12-17','Switzerland','GK', 1,'Inter Milan',     85, 0,183,10000000,false),
  ('CHE','Silvan Widmer',        '1993-03-05','Switzerland','RB', 2,'Mainz',           55, 5,183, 8000000,false),
  ('CHE','Manuel Akanji',        '1995-07-19','Switzerland','CB', 5,'Manchester City', 65, 4,187,35000000,false),
  ('CHE','Nico Elvedi',          '1996-09-30','Switzerland','CB', 4,'Borussia M''gladbach',60,3,187,12000000,false),
  ('CHE','Ricardo Rodriguez',    '1992-08-25','Switzerland','LB', 13,'Torino',         100, 9,175, 5000000,false),
  ('CHE','Granit Xhaka',         '1992-09-27','Switzerland','CM', 10,'Bayer Leverkusen',115,14,185,12000000,true),
  ('CHE','Remo Freuler',         '1992-04-15','Switzerland','CM', 8,'Nottingham Forest',70,10,180, 8000000,false),
  ('CHE','Fabian Rieder',        '2002-04-11','Switzerland','CM', 7,'Stade Rennais',   25, 4,183,18000000,false),
  ('CHE','Ruben Vargas',         '1998-08-05','Switzerland','LW', 11,'Augsburg',       50,10,175,12000000,false),
  ('CHE','Breel Embolo',         '1997-02-14','Switzerland','ST', 9,'Monaco',          60,17,187,15000000,false),
  ('CHE','Noah Okafor',          '2000-05-24','Switzerland','LW', 14,'AC Milan',       30, 7,182,22000000,false),
  ('CHE','Dan Ndoye',            '2000-10-08','Switzerland','RW', 17,'Bologna',        25, 6,184,20000000,false),

  -- Austria (AUT)
  ('AUT','Patrick Pentz',        '1997-01-20','Austria','GK', 1,'Bayer Leverkusen',   30, 0,193, 6000000,false),
  ('AUT','Stefan Lainer',        '1992-08-27','Austria','RB', 2,'Borussia M''gladbach',60,5,176, 4000000,false),
  ('AUT','David Alaba',          '1992-06-24','Austria','CB', 14,'Real Madrid',       100,12,180,12000000,false),
  ('AUT','Maximilian Woeber',    '1998-10-04','Austria','CB', 4,'Leeds United',        35, 2,184,10000000,false),
  ('AUT','Philipp Mwene',        '1997-03-29','Austria','LB', 3,'PSV Eindhoven',      30, 2,178, 6000000,false),
  ('AUT','Nicolas Seiwald',      '2001-08-07','Austria','CDM', 6,'RB Leipzig',        30, 3,181,20000000,false),
  ('AUT','Florian Grillitsch',   '1995-08-07','Austria','CM', 8,'Ajax',               55, 4,190, 6000000,false),
  ('AUT','Konrad Laimer',        '1997-05-27','Austria','CM', 17,'Bayern Munich',     50, 8,175,25000000,false),
  ('AUT','Christoph Baumgartner','1999-08-01','Austria','CAM',10,'RB Leipzig',        40, 9,181,25000000,false),
  ('AUT','Marcel Sabitzer',      '1994-03-17','Austria','CM', 7,'Borussia Dortmund',  75,15,178,15000000,true),
  ('AUT','Michael Gregoritsch',  '1994-04-04','Austria','ST', 9,'Freiburg',           55,18,192, 8000000,false),
  ('AUT','Marko Arnautovic',     '1989-04-19','Austria','CF', 9,'Inter Milan',        115,36,192, 5000000,false),

  -- Saudi Arabia (SAU)
  ('SAU','Mohammed Al-Owais',    '1991-10-15','Saudi Arabia','GK', 1,'Al Hilal',      60, 0,190, 3000000,false),
  ('SAU','Saud Abdulhamid',      '1999-07-22','Saudi Arabia','RB', 2,'Roma',          35, 2,183, 8000000,false),
  ('SAU','Ali Al-Bulayhi',       '1989-09-28','Saudi Arabia','LB', 13,'Al Hilal',     85, 3,184, 2000000,false),
  ('SAU','Hassan Tambakti',      '2000-01-02','Saudi Arabia','CB', 5,'Al Hilal',      30, 1,194, 5000000,false),
  ('SAU','Yasser Al-Shahrani',   '1992-09-29','Saudi Arabia','LB', 3,'Al Hilal',      80, 5,174, 3000000,false),
  ('SAU','Salman Al-Faraj',      '1989-08-01','Saudi Arabia','CDM', 8,'Al Hilal',     115, 9,182, 4000000,true),
  ('SAU','Sami Al-Najei',        '1993-06-05','Saudi Arabia','CM', 6,'Al Qadsiah',    50, 4,176, 2000000,false),
  ('SAU','Mohammed Kanno',       '1997-04-02','Saudi Arabia','CM', 7,'Al Hilal',      50, 6,180, 5000000,false),
  ('SAU','Abdullah Al-Hamdan',   '2000-01-20','Saudi Arabia','ST', 9,'Al Hilal',      30, 8,185, 6000000,false),
  ('SAU','Salem Al-Dawsari',     '1991-08-19','Saudi Arabia','LW', 10,'Al Hilal',     80,22,169, 6000000,false),
  ('SAU','Firas Al-Buraikan',    '1999-05-14','Saudi Arabia','CF', 11,'Al Fateh',     35,12,183, 6000000,false),

  -- Ivory Coast (CIV)
  ('CIV','Yahia Fofana',         '1997-01-19','Ivory Coast','GK', 1,'Leicester City', 35, 0,191, 8000000,false),
  ('CIV','Serge Aurier',         '1992-12-24','Ivory Coast','RB', 2,'Nottingham Forest',75,9,178, 4000000,false),
  ('CIV','Eric Bailly',          '1994-04-12','Ivory Coast','CB', 3,'Besiktas',       60, 2,187, 5000000,false),
  ('CIV','Willy Boly',           '1991-02-03','Ivory Coast','CB', 4,'Nottingham Forest',40,3,194, 4000000,false),
  ('CIV','Franck Kessie',        '1996-12-19','Ivory Coast','CDM', 5,'Al Ahli',       80,14,183,15000000,false),
  ('CIV','Jean-Michael Seri',    '1991-07-19','Ivory Coast','CM', 8,'Galatasaray',    70, 5,168, 4000000,false),
  ('CIV','Seko Fofana',          '1995-05-07','Ivory Coast','CM', 6,'Al Qadsiah',     50, 9,184,15000000,false),
  ('CIV','Nicolas Pepe',         '1995-05-29','Ivory Coast','RW', 19,'Trabzonspor',   75,22,180, 8000000,false),
  ('CIV','Simon Adingra',        '2002-01-07','Ivory Coast','LW', 11,'Brighton',      25,10,170,25000000,false),
  ('CIV','Sebastien Haller',     '1994-06-22','Ivory Coast','ST', 9,'Borussia Dortmund',60,26,193,15000000,true),
  ('CIV','Wilfried Zaha',        '1992-11-10','Ivory Coast','LW', 11,'Galatasaray',   80,24,178, 6000000,false),

  -- =========================================================
  -- GROUP J
  -- =========================================================

  -- South Korea (KOR)
  ('KOR','Kim Seung-gyu',        '1990-01-30','South Korea','GK', 1,'Vissel Kobe',    75, 0,189, 2000000,false),
  ('KOR','Kim Moon-hwan',        '1995-08-01','South Korea','RB', 2,'Jeonbuk Motors', 55, 4,177, 3000000,false),
  ('KOR','Kim Min-jae',          '1996-11-15','South Korea','CB', 3,'Bayern Munich',  65, 5,190,55000000,false),
  ('KOR','Kwon Kyung-won',       '1992-01-31','South Korea','CB', 4,'Al Shabab',      60, 4,185, 4000000,false),
  ('KOR','Kim Jin-su',           '1992-06-13','South Korea','LB', 15,'Jeonbuk Motors',60, 4,180, 2000000,false),
  ('KOR','Jung Woo-young',       '1989-09-20','South Korea','CDM', 16,'Al Qadsiah',   105, 8,182, 3000000,false),
  ('KOR','Lee Jae-sung',         '1992-08-10','South Korea','CM', 7,'Mainz',          85,16,180, 5000000,false),
  ('KOR','Hwang In-beom',        '1996-09-20','South Korea','CM', 8,'Freiburg',       70,14,177, 8000000,false),
  ('KOR','Son Heung-min',        '1992-07-08','South Korea','LW', 7,'Tottenham',      120,50,183,40000000,true),
  ('KOR','Hwang Hee-chan',       '1996-01-26','South Korea','ST', 10,'Wolves',        65,28,176,15000000,false),
  ('KOR','Lee Kang-in',          '2001-02-19','South Korea','CAM',17,'PSG',           40,12,173,35000000,false),
  ('KOR','Cho Gue-sung',         '1998-01-25','South Korea','CF', 9,'Jeonbuk Motors', 30,14,185, 5000000,false),

  -- Iran (IRN)
  ('IRN','Alireza Beiranvand',   '1992-09-21','Iran','GK', 1,'Persepolis',            75, 0,193, 3000000,false),
  ('IRN','Sadegh Moharrami',     '1997-12-04','Iran','RB', 2,'Dinamo Zagreb',         55, 2,184, 5000000,false),
  ('IRN','Morteza Pouraliganji', '1993-10-02','Iran','CB', 5,'Al Gharafa',            70, 5,190, 3000000,false),
  ('IRN','Majid Hosseini',       '1997-07-20','Iran','CB', 4,'Kasimpasa',             45, 2,185, 4000000,false),
  ('IRN','Ehsan Hajsafi',        '1990-02-25','Iran','LB', 3,'AEK Athens',            105, 9,179, 3000000,false),
  ('IRN','Saeid Ezatolahi',      '1996-10-01','Iran','CDM', 6,'Cercle Brugge',        65, 6,183, 5000000,false),
  ('IRN','Ahmad Noorollahi',     '1994-09-04','Iran','CM', 8,'Persepolis',            60, 5,180, 2000000,false),
  ('IRN','Ali Gholizadeh',       '1996-01-10','Iran','RW', 11,'Charleroi',            55,14,178, 5000000,false),
  ('IRN','Sardar Azmoun',        '1995-01-01','Iran','ST', 23,'AS Roma',              68,42,187,15000000,false),
  ('IRN','Mehdi Taremi',         '1992-07-18','Iran','CF', 9,'Inter Milan',           90,48,187,20000000,true),
  ('IRN','Allahyar Sayyadmanesh','2001-02-12','Iran','LW', 17,'Jacksonville',         35,10,183, 6000000,false),

  -- Poland (POL)
  ('POL','Wojciech Szczesny',    '1990-04-18','Poland','GK', 1,'Barcelona',           95, 0,196,10000000,false),
  ('POL','Matty Cash',           '1997-08-07','Poland','RB', 2,'Aston Villa',         45, 5,183,20000000,false),
  ('POL','Jan Bednarek',         '1996-04-12','Poland','CB', 5,'Aston Villa',         60, 3,189,18000000,false),
  ('POL','Jakub Kiwior',         '2000-02-15','Poland','CB', 15,'Arsenal',            30, 1,186,15000000,false),
  ('POL','Bartosz Bereszynski',  '1992-07-12','Poland','RB', 23,'Sampdoria',          65, 3,182, 3000000,false),
  ('POL','Piotr Zielinski',      '1994-06-20','Poland','CM', 10,'Inter Milan',        90,25,180,30000000,false),
  ('POL','Grzegorz Krychowiak',  '1990-01-29','Poland','CDM', 8,'Al Shabab',          100, 9,185, 4000000,false),
  ('POL','Sebastian Szymanski',  '1999-05-10','Poland','CAM', 7,'Fenerbahce',         40,10,181,20000000,false),
  ('POL','Karol Swiderski',      '1997-01-23','Poland','ST', 9,'Charlotte FC',        45,15,184,10000000,false),
  ('POL','Robert Lewandowski',   '1988-08-21','Poland','CF', 9,'Barcelona',           155,82,185,15000000,true),
  ('POL','Arkadiusz Milik',      '1994-02-28','Poland','ST', 23,'Juventus',           65,25,186,12000000,false),
  ('POL','Przemyslaw Frankowski', '1995-04-12','Poland','RW', 16,'RC Lens',           50, 8,177,12000000,false),

  -- Cameroon (CMR)
  ('CMR','Andre Onana',          '1996-04-02','Cameroon','GK', 1,'Manchester United', 55, 0,190,25000000,false),
  ('CMR','Harold Moukoudi',      '1997-01-07','Cameroon','CB', 3,'Saint-Etienne',     30, 1,192, 3000000,false),
  ('CMR','Olivier Mbaizo',       '1998-06-25','Cameroon','RB', 2,'Philadelphia Union', 30, 1,178, 3000000,false),
  ('CMR','Nouhou Tolo',          '1994-11-16','Cameroon','LB', 18,'Seattle Sounders', 45, 2,177, 3000000,false),
  ('CMR','Enzo Ebosse',          '1999-03-01','Cameroon','CB', 5,'Udinese',           25, 1,186, 5000000,false),
  ('CMR','Andre Zambo Anguissa', '1995-11-16','Cameroon','CDM', 8,'Napoli',           65,10,183,30000000,false),
  ('CMR','Martin Hongla',        '1998-10-15','Cameroon','CM', 10,'Hellas Verona',    35, 3,180, 5000000,false),
  ('CMR','Karl Toko Ekambi',     '1992-09-14','Cameroon','LW', 17,'Lyon',             70,20,184,10000000,false),
  ('CMR','Bryan Mbeumo',         '1999-08-07','Cameroon','RW', 11,'Brentford',        35,18,174,35000000,false),
  ('CMR','Vincent Aboubakar',    '1992-01-22','Cameroon','ST', 9,'Al Qadsiah',        100,41,184,10000000,true),
  ('CMR','Eric Maxim Choupo-Moting','1989-03-23','Cameroon','CF', 23,'Besiktas',      80,28,190, 4000000,false),
  ('CMR','Jean-Charles Castelletto','1995-01-25','Cameroon','CB', 4,'Nantes',         40, 2,185, 4000000,false),

  -- =========================================================
  -- GROUP K
  -- =========================================================

  -- Denmark (DNK)
  ('DNK','Kasper Schmeichel',    '1986-11-05','Denmark','GK', 1,'Anderlecht',         100, 0,189, 3000000,false),
  ('DNK','Alexander Bah',        '1997-08-09','Denmark','RB', 22,'Benfica',           40, 3,183,10000000,false),
  ('DNK','Andreas Christensen',  '1996-04-10','Denmark','CB', 6,'Barcelona',          75, 3,185,30000000,false),
  ('DNK','Jannik Vestergaard',   '1992-08-03','Denmark','CB', 4,'Leicester City',     65, 5,197,10000000,false),
  ('DNK','Joakim Maehle',        '1997-05-20','Denmark','LB', 5,'Atalanta',           55, 8,185,15000000,false),
  ('DNK','Christian Eriksen',    '1992-02-14','Denmark','CAM',10,'Manchester United', 120,38,182,10000000,true),
  ('DNK','Pierre-Emile Hojbjerg','1995-08-05','Denmark','CDM', 23,'Marseille',        80,10,185,18000000,false),
  ('DNK','Mathias Jensen',       '1996-01-01','Denmark','CM', 8,'Brentford',          50, 7,177, 8000000,false),
  ('DNK','Andreas Skov Olsen',   '1999-12-29','Denmark','RW', 19,'Club Brugge',       40,12,181,18000000,false),
  ('DNK','Rasmus Hojlund',       '2003-02-04','Denmark','ST', 11,'Manchester United', 30,12,191,70000000,false),
  ('DNK','Kasper Dolberg',       '1997-10-06','Denmark','CF', 20,'Eintracht Frankfurt',50,18,187,10000000,false),
  ('DNK','Christian Norgaard',   '1994-03-10','Denmark','CDM',16,'Brentford',          55, 5,189, 8000000,false),

  -- Hungary (HUN)
  ('HUN','Peter Gulacsi',        '1990-05-06','Hungary','GK', 1,'RB Leipzig',         65, 0,190,10000000,false),
  ('HUN','Attila Fiola',         '1990-02-17','Hungary','RB', 2,'Puskas Akademia',    75, 5,180, 2000000,false),
  ('HUN','Willi Orban',          '1992-11-03','Hungary','CB', 4,'RB Leipzig',         65, 6,184,10000000,false),
  ('HUN','Adam Lang',            '1993-01-17','Hungary','CB', 5,'Omonia',             55, 3,189, 2000000,false),
  ('HUN','Mihaly Kata',          '1997-02-27','Hungary','LB', 3,'Puskas Akademia',    30, 1,177,  800000,false),
  ('HUN','Adam Nagy',            '1995-06-17','Hungary','CDM', 6,'Bristol City',      65, 5,177, 3000000,false),
  ('HUN','Dominik Szoboszlai',   '2000-10-25','Hungary','CM', 10,'Liverpool',         55,20,186,70000000,true),
  ('HUN','Roland Sallai',        '1997-05-22','Hungary','RW', 17,'Freiburg',          55,16,180,12000000,false),
  ('HUN','Barnabas Varga',       '1994-09-23','Hungary','ST', 9,'Ferencvaros',        70,24,186, 5000000,false),
  ('HUN','Martin Adam',          '1997-11-06','Hungary','CF', 19,'Vasas FC',          30,10,191, 2000000,false),
  ('HUN','Kevin Csoboth',        '2001-08-25','Hungary','LW', 11,'Puskas Akademia',   20, 4,179, 2000000,false),

  -- Slovakia (SVK)
  ('SVK','Martin Dubravka',      '1989-01-15','Slovakia','GK', 1,'Newcastle United',  75, 0,191, 8000000,false),
  ('SVK','Peter Pekarik',        '1986-10-30','Slovakia','RB', 2,'Hertha Berlin',     120, 6,175, 1000000,false),
  ('SVK','Milan Skriniar',       '1995-02-11','Slovakia','CB', 5,'PSG',               75, 5,187,35000000,false),
  ('SVK','Denis Vavro',          '1996-09-10','Slovakia','CB', 4,'Kopenhagen',        40, 2,191, 8000000,false),
  ('SVK','David Hancko',         '1997-12-13','Slovakia','CB', 3,'Feyenoord',         35, 3,184,20000000,false),
  ('SVK','Juraj Kucka',          '1987-02-26','Slovakia','CDM', 8,'Trabzonspor',      110, 9,184, 2000000,false),
  ('SVK','Stanislav Lobotka',    '1994-11-25','Slovakia','CM', 10,'Napoli',           75, 4,170,30000000,false),
  ('SVK','Ondrej Duda',          '1994-12-05','Slovakia','CAM', 7,'Norwich City',     75,18,181, 5000000,false),
  ('SVK','Ivan Schranz',         '1993-07-28','Slovakia','RW', 19,'Slavia Prague',    45,14,181, 8000000,false),
  ('SVK','Robert Bozenik',       '1999-09-18','Slovakia','ST', 9,'Feyenoord',         40,15,190,10000000,false),
  ('SVK','Lukas Haraslin',       '1996-05-26','Slovakia','LW', 11,'Sassuolo',         45,12,179, 6000000,false),
  ('SVK','Tomas Suslov',         '2002-08-02','Slovakia','CAM',17,'Hellas Verona',    20, 4,183,10000000,true),

  -- South Africa (ZAF)
  ('ZAF','Ronwen Williams',      '1992-08-26','South Africa','GK', 1,'Mamelodi Sundowns',80, 0,186, 1500000,false),
  ('ZAF','Reeve Frosler',        '1998-01-11','South Africa','RB', 2,'Genk',           35, 2,175, 4000000,false),
  ('ZAF','Siyanda Xulu',         '1990-12-20','South Africa','CB', 5,'Kaizer Chiefs',  70, 4,190, 1000000,false),
  ('ZAF','Rushine de Reuck',     '1996-08-15','South Africa','CB', 4,'Motherwell',     40, 2,192, 2000000,false),
  ('ZAF','Mothobi Mvala',        '1997-12-18','South Africa','CDM', 6,'Mamelodi Sundowns',40, 3,185, 2000000,false),
  ('ZAF','Teboho Mokoena',       '1997-09-29','South Africa','CM', 8,'Mamelodi Sundowns',45, 6,177, 3000000,false),
  ('ZAF','Grant Kekana',         '1993-05-04','South Africa','CB', 3,'Mamelodi Sundowns',40, 4,187, 1500000,false),
  ('ZAF','Percy Tau',            '1994-05-13','South Africa','LW', 11,'Al Ahly',       70,22,172, 4000000,false),
  ('ZAF','Evidence Makgopa',     '2000-01-09','South Africa','ST', 9,'Orlando Pirates', 30,10,183, 3000000,false),
  ('ZAF','Lyle Foster',          '2001-04-05','South Africa','CF', 23,'Burnley',        25, 8,184, 6000000,true),
  ('ZAF','Themba Zwane',         '1993-03-21','South Africa','CAM',10,'Mamelodi Sundowns',70,20,173, 2000000,false),

  -- =========================================================
  -- GROUP L
  -- =========================================================

  -- Slovenia (SVN)
  ('SVN','Jan Oblak',            '1993-01-07','Slovenia','GK', 1,'Atletico Madrid',   75, 0,188,20000000,true),
  ('SVN','Jure Balkovec',        '1994-05-09','Slovenia','LB', 3,'Rakow Czestochowa', 50, 3,181, 3000000,false),
  ('SVN','Miha Blazic',          '1993-05-08','Slovenia','CB', 5,'Ferencvaros',       50, 4,188, 4000000,false),
  ('SVN','Jaka Bijol',           '1999-02-05','Slovenia','CB', 4,'Udinese',           35, 2,188,10000000,false),
  ('SVN','Zan Karnicnik',        '1994-10-01','Slovenia','RB', 2,'Celje',             45, 3,180, 2000000,false),
  ('SVN','Adam Cerin',           '1999-11-17','Slovenia','CM', 8,'Udinese',           30, 3,178, 5000000,false),
  ('SVN','Timi Max Elsnik',      '1996-07-18','Slovenia','CM', 10,'Hajduk Split',     35, 5,178, 3000000,false),
  ('SVN','Sandi Lovric',         '2000-10-23','Slovenia','CM', 17,'Udinese',          25, 4,181, 6000000,false),
  ('SVN','Benjamin Sesko',       '2003-05-31','Slovenia','ST', 9,'RB Leipzig',        30,14,194,60000000,false),
  ('SVN','Andraz Sporar',        '1994-02-27','Slovenia','CF', 11,'Sporting CP',      55,16,183, 8000000,false),
  ('SVN','Jon Gorenc Stankovic', '1996-03-09','Slovenia','CDM', 6,'Sturm Graz',       45, 2,188, 4000000,false),

  -- Uzbekistan (UZB)
  ('UZB','Eldorbek Suyunov',     '1995-04-18','Uzbekistan','GK', 1,'Pakhtakor',       55, 0,186, 1200000,false),
  ('UZB','Shuhrat Mukhammadiev', '1999-01-12','Uzbekistan','RB', 2,'Pakhtakor',       30, 1,178, 1000000,false),
  ('UZB','Jamshid Iskanderov',   '1996-06-08','Uzbekistan','CB', 4,'Pakhtakor',       40, 2,187, 1000000,false),
  ('UZB','Umid Nishonov',        '1997-03-20','Uzbekistan','CB', 5,'Pakhtakor',       35, 2,185,  900000,false),
  ('UZB','Mansur Jaloliddinov',  '1998-08-14','Uzbekistan','LB', 3,'Navbahor',        35, 2,179,  800000,false),
  ('UZB','Otabek Shukurov',      '1994-07-05','Uzbekistan','CDM', 6,'Pakhtakor',      50, 3,180,  900000,false),
  ('UZB','Dostonbek Khamdamov',  '2001-03-15','Uzbekistan','CM', 8,'Pakhtakor',       25, 4,177, 1200000,false),
  ('UZB','Jaloliddin Masharipov','1993-12-06','Uzbekistan','CAM',10,'CSKA Moscow',    70,20,170, 3000000,false),
  ('UZB','Eldor Shomurodov',     '1995-06-29','Uzbekistan','ST', 9,'Roma',            60,22,187,10000000,true),
  ('UZB','Sherzod Nasrullayev',  '2000-04-10','Uzbekistan','RW', 17,'Pakhtakor',      20, 5,175, 1000000,false),
  ('UZB','Akbar Qodirov',        '1999-11-22','Uzbekistan','LW', 11,'Navbahor',       25, 4,176,  800000,false),

  -- Paraguay (PRY)
  ('PRY','Antony Silva',         '1984-07-14','Paraguay','GK', 1,'Guarani',           90, 0,185, 1000000,false),
  ('PRY','Robert Rojas',         '1997-07-18','Paraguay','RB', 2,'River Plate',       55, 4,181, 8000000,false),
  ('PRY','Omar Alderete',        '1996-12-26','Paraguay','CB', 3,'Getafe',            35, 2,184, 6000000,false),
  ('PRY','Gustavo Gomez',        '1993-06-06','Paraguay','CB', 5,'Palmeiras',         65, 8,186, 5000000,true),
  ('PRY','Santiago Arzamendia',  '1997-03-22','Paraguay','LB', 23,'Cadiz',            45, 3,171, 5000000,false),
  ('PRY','Miguel Almiron',       '1994-02-10','Paraguay','CAM',10,'Newcastle United', 75,22,175,15000000,false),
  ('PRY','Mathias Villasanti',   '1998-11-01','Paraguay','CDM', 8,'Gremio',           35, 3,181, 8000000,false),
  ('PRY','Andres Cubas',         '1997-01-26','Paraguay','CM', 6,'Nottingham Forest', 35, 2,182, 8000000,false),
  ('PRY','Carlos Gonzalez',      '1994-03-06','Paraguay','RW', 11,'LAFC',             50,14,178, 5000000,false),
  ('PRY','Antonio Sanabria',     '1996-03-04','Paraguay','ST', 9,'Torino',            55,18,181, 8000000,false),
  ('PRY','Julio Enciso',         '2004-01-23','Paraguay','RW', 14,'Brighton',         25,10,170,20000000,false),

  -- Romania (ROU)
  ('ROU','Florin Nita',          '1987-07-03','Romania','GK', 1,'Damac FC',           65, 0,191, 1500000,false),
  ('ROU','Andrei Ratiu',         '1999-12-13','Romania','RB', 2,'Villarreal',         35, 3,181,10000000,false),
  ('ROU','Radu Dragusin',        '2002-02-03','Romania','CB', 4,'Tottenham',          30, 1,194,25000000,false),
  ('ROU','Ionut Nedelcearu',     '1996-02-11','Romania','CB', 5,'Palermo',            40, 2,187, 4000000,false),
  ('ROU','Nicusor Bancu',        '1992-06-18','Romania','LB', 3,'Universitatea Craiova',60, 5,176, 1500000,false),
  ('ROU','Marius Marin',         '1997-07-31','Romania','CM', 8,'Pisa',               35, 2,173, 5000000,false),
  ('ROU','Razvan Marin',         '1996-05-26','Romania','CM', 18,'Cagliari',          65,10,183, 8000000,false),
  ('ROU','Nicolae Stanciu',      '1993-05-07','Romania','CAM',10,'Damac FC',          80,26,175, 5000000,true),
  ('ROU','Florin Tanase',        '1994-12-31','Romania','LW', 11,'Al Jazeera',        50,18,177, 4000000,false),
  ('ROU','Denis Alibec',         '1991-01-05','Romania','ST', 9,'Farul Constanta',    50,16,186, 2000000,false),
  ('ROU','Valentin Mihaila',     '2000-08-15','Romania','RW', 17,'Atalanta',          30, 8,176,12000000,false)

) AS v(iso3, name, dob, nationality, pos, num, club, caps, goals, ht, mkt, cap)
  ON ti.code_iso3 = v.iso3;
