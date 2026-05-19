-- Tabela de controle (criar se não existir)
CREATE TABLE IF NOT EXISTS db_seeds (
    nome        VARCHAR(100) PRIMARY KEY,
    executado_em TIMESTAMP DEFAULT NOW()
);

-- Garantia de unicidade case-insensitive no banco (segunda linha de defesa)
-- UNIQUE constraint padrão é case-sensitive; índice funcional com LOWER() cobre variações de capitalização
ALTER TABLE reservas DROP CONSTRAINT IF EXISTS uq_reservas_nome;
CREATE UNIQUE INDEX IF NOT EXISTS uq_reservas_nome_ci ON reservas (LOWER(nome));

-- Só executa o seed se ainda não foi registrado
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM db_seeds WHERE nome = 'seed_convidados_v1') THEN

    INSERT INTO reservas (nome, tipo, valor, pago, criado_em, atualizado_em) VALUES
    ('Andrey e Lara',               'CASAL',      100.00, false, NOW(), NOW()),
    ('Cristiane e Pedro',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Rubens e Alda',               'CASAL',      100.00, false, NOW(), NOW()),
    ('Paulo e Márcia Vasconcelos',  'CASAL',      100.00, true,  NOW(), NOW()),
    ('Dorvanir e Socorro',          'CASAL',      100.00, true,  NOW(), NOW()),
    ('Mário e Fracianne',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Jairo e Cléa',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('Aurijones e Adriana',         'CASAL',      100.00, false, NOW(), NOW()),
    ('Jackson e Vera',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Érico e Elaine',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Wagner e Renatha',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('César e Edna',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('Gilberto e Márcia',           'CASAL',      100.00, false, NOW(), NOW()),
    ('Ciria e Juliel',              'CASAL',      100.00, false, NOW(), NOW()),
    ('Giovani (+1)',                'CASAL',      100.00, false, NOW(), NOW()),
    ('Gelber e Gaby',               'CASAL',      100.00, false, NOW(), NOW()),
    ('Felipe e Ingrid',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Charles e Dayra',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Yuri e Sissi',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('João Pedro e Thaís',          'CASAL',      100.00, true,  NOW(), NOW()),
    ('André e Fernanda',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('Marta e Alcemir',             'CASAL',      100.00, false, NOW(), NOW()),
    ('Francisco e Venina',          'CASAL',      100.00, false, NOW(), NOW()),
    ('Jadel e Cíntia',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Leonardo e Karla',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('Jander e Odemila',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('Francisco e Lígia Maria',     'CASAL',      100.00, true,  NOW(), NOW()),
    ('Ramon e Marcele',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Júnior e Micilene',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('André e Giselle',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Jorjão',                      'INDIVIDUAL',  50.00, false, NOW(), NOW()),
    ('Fredman e Dyenni',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('Kennedy e Karol',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('André e Eduarda',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Carlos e Patrícia',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Udson e Cris',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('George e Sara',               'CASAL',      100.00, true,  NOW(), NOW()),
    ('Severino e Elem',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Carlos e Keila Pinheiro',     'CASAL',      100.00, true,  NOW(), NOW()),
    ('Artur e Edna',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('João e Val',                  'CASAL',      100.00, true,  NOW(), NOW()),
    ('Paulo e Josefa',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Arildo e Sandra',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('José Antônio e Layla',        'CASAL',      100.00, true,  NOW(), NOW()),
    ('Eduardo e Mara',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('José Reinaldo e Odete',       'CASAL',      100.00, true,  NOW(), NOW()),
    ('José Gabriel e Luna',         'CASAL',      100.00, true,  NOW(), NOW()),
    ('Luiz Felipe e Lúcia',         'CASAL',      100.00, true,  NOW(), NOW()),
    ('Dafne e Márcia',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Lima Filho e Josi',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Marcos Alexandre e Sarah',    'CASAL',      100.00, true,  NOW(), NOW()),
    ('Miguel e Marina',             'CASAL',      100.00, true,  NOW(), NOW()),
    ('Luís e Chaguinha',            'CASAL',      100.00, false, NOW(), NOW()),
    ('Tavin e Terezinha',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Adinaldo e Camila',           'CASAL',      100.00, false, NOW(), NOW()),
    ('Alberto e Sara',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Zezinho e Kátia',             'CASAL',      100.00, false, NOW(), NOW()),
    ('Wallace e Yonara',            'CASAL',      100.00, true,  NOW(), NOW()),
    ('Ige e Ivo',                   'CASAL',      100.00, true,  NOW(), NOW()),
    ('Felipe e Railda',             'CASAL',      100.00, false, NOW(), NOW()),
    ('Witiley e Renata',            'CASAL',      100.00, false, NOW(), NOW()),
    ('Evandro e Meire',             'CASAL',      100.00, false, NOW(), NOW()),
    ('Andre e Elen Bruna',          'CASAL',      100.00, true,  NOW(), NOW()),
    ('Otávio e Ester',              'CASAL',      100.00, false, NOW(), NOW()),
    ('Zenon e Ariane',              'CASAL',      100.00, false, NOW(), NOW()),
    ('Edvaldo e Maria',             'CASAL',      100.00, false, NOW(), NOW()),
    ('Daniel e Geiza',              'CASAL',      100.00, true,  NOW(), NOW()),
    ('Dudu e Laura',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('Léo e Ionara',                'CASAL',      100.00, false, NOW(), NOW()),
    ('Henrique e Marise',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Plácido e Rebecca',           'CASAL',      100.00, true,  NOW(), NOW()),
    ('Fátima Vasconcelos',          'INDIVIDUAL',  50.00, true,  NOW(), NOW()),
    ('Leno e Dalva',                'CASAL',      100.00, true,  NOW(), NOW()),
    ('Aurisfran e Dorotéia',        'CASAL',      100.00, false, NOW(), NOW()),
    ('Zé Maria e Nazaré',           'CASAL',      100.00, false, NOW(), NOW());

    INSERT INTO db_seeds (nome) VALUES ('seed_convidados_v1');

  END IF;
END $$;
