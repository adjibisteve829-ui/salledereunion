# Gestion des salles de réunion

Application web Jakarta EE 10 (Java 17), avec PostgreSQL et authentification JAAS.
Elle est prévue pour une version de WildFly compatible avec Jakarta EE 10.

### Prérequis de compilation et d'exécution

- JDK 17 ou ultérieur pour compiler l'application et exécuter WildFly.
- Maven 3.9 ou ultérieur.
- Une version de WildFly compatible avec Jakarta EE 10.

Le projet configure Maven Compiler avec `maven.compiler.release=17`.

## PostgreSQL

Les entités JPA sont définies dans `src/main/java/bj/salledereunion/model` et l'unité de
persistance `salledereunion` dans `src/main/resources/META-INF/persistence.xml`.
WildFly fournit le fournisseur Hibernate/JPA ; le pilote PostgreSQL est inclus dans le WAR.
La vérification du compte utilise JDBC direct ; les autres opérations continuent d'utiliser
JPA. Les deux utilisent le fichier de configuration local décrit ci-dessous.

Créez la base si elle n'existe pas, puis exécutez `src/main/resources/schema.sql` :

```sql
CREATE DATABASE salledereunion;
```

```powershell
psql -U postgres -d salledereunion -f src\main\resources\schema.sql
```

L'application utilise toujours JAAS pour l'authentification (`LoginContext` et
`UtilisateurLoginModule`). Crée le fichier `%USERPROFILE%\.salledereunion\db.properties` :

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.salledereunion"
notepad "$env:USERPROFILE\.salledereunion\db.properties"
```

```properties
db.url=jdbc:postgresql://localhost:5432/salledereunion
db.user=postgres
db.password=votre-mot-de-passe-postgresql
```

Remplace la valeur d'exemple par le mot de passe défini lors de l'installation de PostgreSQL.
Ce fichier reste hors du projet et n'est pas intégré au WAR. WildFly doit s'exécuter sous le
même compte Windows que celui qui possède ce fichier.

## Créer le premier compte administrateur

Compilez l'application, puis générez un hachage PBKDF2 à partir d'un terminal interactif :

```powershell
mvn clean compile
& "$env:JAVA_HOME\bin\java.exe" -cp target\classes bj.salledereunion.util.PasswordUtil
```

Vérifiez que `JAVA_HOME` désigne un JDK 17 ou ultérieur pour la compilation et la génération du hachage.

Insérez le hachage affiché dans PostgreSQL (ne stockez pas le mot de passe en clair) :

```sql
INSERT INTO utilisateur (nom, email, mot_de_passe, niveau_hierarchique, role)
VALUES ('Administrateur', 'admin@example.com', '<hachage-genere>', 0, 'ADMINISTRATEUR');
```

Les rôles acceptés sont `UTILISATEUR`, `AUTORITE` et `ADMINISTRATEUR`. Les niveaux des autorités sont des entiers :
un niveau strictement supérieur est prioritaire. La connexion passe par un `LoginModule` JAAS applicatif,
et les filtres servlet protègent les pages selon l'identité et le rôle déposés dans la session.

## Administration

Depuis **Administration**, un compte administrateur peut créer, modifier et supprimer des salles
et des comptes, gérer les réservations, et régler les délais de préemption. Les mots de passe
des comptes sont hachés avec PBKDF2 avant stockage. Le compte connecté ne peut pas être supprimé
ou rétrogradé ; le dernier administrateur ne peut pas être supprimé ni converti en un autre rôle.
Les salles et les utilisateurs liés à l'historique des réservations ou des notifications ne
peuvent pas être supprimés. Une réservation clôturée ne peut pas être modifiée ; son annulation
est conservée comme historique. Les réservations impliquées dans une préemption active ne sont
pas modifiables.
L'administration affiche les réservations des comptes clients et autorités ; les administrateurs
ne peuvent pas créer ni posséder de réservations.

Après mise à jour de l'application, exécutez le schéma pour ajouter les paramètres de préemption
et appliquer la nouvelle plage autorisée des délais de notification :

```powershell
psql -U postgres -d salledereunion -f src\main\resources\schema.sql
```

Le formulaire **Paramètres** permet à chaque utilisateur de modifier son nom, son adresse e-mail
et son mot de passe ; les rôles et niveaux hiérarchiques restent sous contrôle administratif.

Si les équipements des salles affichent des caractères comme `Ã‰cran` ou `visioconfÃ©rence`,
relance le script `src/main/resources/schema.sql` avec la commande ci-dessus. Il corrige ces
valeurs initiales corrompues sans écraser les équipements personnalisés.

## Espace client

Depuis la page de connexion, un visiteur peut créer un compte. L'inscription active immédiatement
un compte de rôle `UTILISATEUR` ; le mot de passe doit comporter au moins 12 caractères et est
stocké sous forme de hachage PBKDF2. Le tableau de bord présente les prochaines réservations du
client et ses notifications. Dans **Salles**, il peut rechercher des disponibilités sur un créneau
futur ; **Réservations** lui permet de créer une réservation et de consulter ou annuler ses propres
créneaux confirmés avant leur début. L'historique est conservé après annulation.

## Compiler et déployer

```powershell
mvn clean package
```

Définissez `JAVA_HOME` sur un JDK 17 ou ultérieur, puis construisez le WAR :

```powershell
$env:JAVA_HOME = "C:\chemin\vers\jdk-25"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Déployez `target\salledereunion.war` dans le dossier `standalone\deployments` de WildFly :

```powershell
Copy-Item target\salledereunion.war "C:\chemin\vers\wildfly\standalone\deployments\"
```

Ouvrez `http://localhost:8080/salledereunion/` ou
`http://localhost:8080/salledereunion/login`. Consultez `wildfly\standalone\log\server.log`
pour vérifier le déploiement et diagnostiquer les erreurs.

### Déploiement d'essai sur Render

Le fichier `render.yaml` décrit un service web Docker et une base PostgreSQL gratuits dans la
région de Francfort. Le `Dockerfile` compile le WAR avec Java 17 et l'exécute sur WildFly.
Poussez ces fichiers dans le dépôt GitHub, puis créez une ressource **Blueprint** sur Render
et sélectionnez ce dépôt. Render créera les deux ressources et fournira les paramètres PostgreSQL
au service via des variables d'environnement ; aucun mot de passe n'est stocké dans le dépôt.

Le service web gratuit s'endort après 15 minutes sans trafic et peut mettre environ une minute
à redémarrer. La base gratuite est limitée à 1 Go et expire après 30 jours ; elle convient
uniquement à un essai. Exportez les données avant l'expiration ou passez la base à un plan payant
si elles doivent rester disponibles.

#### Migrer les données PostgreSQL locales

Avant le premier déploiement, créez un export complet de la base locale dans PowerShell :

```powershell
pg_dump --format=custom --host localhost --port 5432 --username postgres `
    --dbname salledereunion --file "$env:TEMP\salledereunion.dump"
```

Une fois la base Render créée, récupérez son **External Database URL** dans **Connect**. Utilisez
cette URL externe depuis votre ordinateur pour restaurer l'export dans la base Render encore vide :

```powershell
$env:RENDER_DATABASE_URL = Read-Host "URL externe de PostgreSQL Render"
pg_restore --no-owner --no-acl --dbname="$env:RENDER_DATABASE_URL" `
    "$env:TEMP\salledereunion.dump"
Remove-Item Env:\RENDER_DATABASE_URL
```

L'URL contient un secret : ne la commitez pas et ne la partagez pas. Conservez l'export local
jusqu'à ce que l'application et les données restaurées soient vérifiées.

### WildFly affiche « Le service d'authentification est temporairement indisponible »

L'application utilise JAAS (`LoginContext` et `UtilisateurLoginModule`) ; le module vérifie
le compte avec JDBC dans PostgreSQL. Vérifie que le fichier
`%USERPROFILE%\.salledereunion\db.properties` est présent et contient `db.url`, `db.user` et
`db.password`, que PostgreSQL est démarré et que le schéma existe. Consulte
`standalone\log\server.log` pour voir la cause technique de l'échec JAAS.
Un e-mail ou mot de passe utilisateur invalide doit afficher « Adresse e-mail ou mot de passe
incorrect », pas cette erreur 503.

## Préemption

Une autorité dont le niveau dépasse celui de tous les occupants peut réserver une salle déjà réservée.
Chaque occupant reçoit une notification persistée. Par défaut, le délai est de 60 minutes pour
l'autorité de niveau maximal et de 30 minutes sinon ; ces valeurs et le seuil de libération (60 minutes
par défaut) sont modifiables dans l'administration. Le traitement périodique libère une réservation
s'il lui reste au plus le seuil configuré ;
sinon, il réduit de moitié sa durée restante. La demande prioritaire est confirmée dès que le créneau
n'est plus en conflit. Les demandes ordinaires en conflit sont refusées.

Le statut d'une salle est recalculé comme **Réservée** pour un créneau confirmé à venir,
**Occupée** pendant une réservation confirmée en cours, et **Disponible** lorsqu'aucun créneau actif
ou futur ne la bloque.
