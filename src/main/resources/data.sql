-- Mot de passe: admin123 (haché avec BCrypt)
INSERT INTO users (pseudo, password, is_admin) VALUES
    ('admin', '$2a$10$8.UnVuG9HHPz/h6q9S3NjuQQZrAXNHfPOHOGrNzVKtNVOXQDQQK6a', true);

-- Mot de passe: user123 (haché avec BCrypt)
INSERT INTO users (pseudo, password, is_admin) VALUES
    ('user1', '$2a$10$4ZGU8hVCR5rKGOxZ8iUqaeYuKP2MdlJLJpKYHrVYJ2RGSr1vwrIFK', false);

-- Mot de passe: user456 (haché avec BCrypt)
INSERT INTO users (pseudo, password, is_admin) VALUES
    ('user2', '$2a$10$vKKuRGGwVpGrGkXH1sOKwuJUzVJNIxgKfZ6S2YKlTYANFPB8fXSJa', false);

-- Insertion de tickets de test
INSERT INTO tickets (titre, description, resolved, created_at, created_by) VALUES
    ('Problème de connexion', 'Impossible de se connecter à l''application depuis ce matin', false, CURRENT_TIMESTAMP, 2);

INSERT INTO tickets (titre, description, resolved, created_at, created_by) VALUES
    ('Bug dans le formulaire', 'Le formulaire de contact ne fonctionne pas correctement', false, CURRENT_TIMESTAMP, 3);

INSERT INTO tickets (titre, description, resolved, created_at, created_by, resolved_by, resolved_at) VALUES
    ('Mise à jour réussie', 'La mise à jour de la base de données a été effectuée avec succès', true, CURRENT_TIMESTAMP, 2, 1, CURRENT_TIMESTAMP);