# TP 3 REST — Camel Chafii

Application Jakarta REST (JAX-RS) avec LangChain4j et Gemini, en suivant
l’organisation du TP 2 : Maven, Java 25, Jakarta EE 11 et Payara 7.

Énoncé : http://richard.grin.free.fr/emsi/casablanca-ia/tp/tp3-rest/index.html

## Structure

- `src/main/java/ma/emsi/chafii/tp3rest/GuideApplication.java` : racine `/api`.
- `resource/GuideTouristiqueResource.java` : endpoint `/guide/lieu/{ville_ou_pays}`.
- `llm/GuideTouristique.java` : interface LangChain4j et rôle système en français.
- `llm/LlmClientForGuideTouristique.java` : client Gemini injecté avec CDI.
- `resource/HelloResource.java` : exercice préparatoire JAX-RS.
- `src/main/webapp/WEB-INF/beans.xml` : découverte CDI.
- `src/test` : tests Java ; `tests` : vérifications HTTP avec une API simulée.

Il s’agit d’un service REST : aucune page JSF n’est nécessaire.
Le chemin retenu est celui des consignes détaillées et de l’exemple de test,
`/api/guide/lieu/...`, malgré le chemin différent évoqué dans l’introduction.

## Lancer

Prérequis : JDK 25, Maven (ou celui d’IntelliJ), connexion Internet pour les
dépendances et une clé Gemini. Les versions Maven reprennent celles du TP 2.

```bash
./configurer-gemini.sh
./lancer.sh
```

Le script de configuration demande la clé Gemini avec une saisie masquée et le nom
du modèle. Il les conserve dans `.env`, accessible seulement au propriétaire et
exclu de Git. `lancer.sh` charge ce fichier automatiquement. On peut aussi définir
`GEMINI_API_KEY` ou `GEMINI_KEY` dans l’environnement avant le lancement.
Ne jamais enregistrer une clé dans Git. `GEMINI_MODEL` est obligatoire afin de
choisir un modèle réellement disponible pour le compte utilisé.

Le script compile, exécute les tests, télécharge Payara Micro 7.2026.11 si
nécessaire, puis déploie le WAR sur le port **18085**. Arrêt : Ctrl+C.
Variables facultatives : `MVN_BIN`, `MAVEN_ARGS`, `PAYARA_MICRO_JAR`, `TP3_PORT`.
`./lancer.sh --verifier` compile et exécute les tests sans lancer le serveur.

Sur un serveur Payara existant, déployer `target/tp3-rest-chafii.war` et adapter
le port de l’URL (habituellement 8080). Le serveur doit hériter des variables Gemini.

## Tester le TP et le bonus

```text
http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Nice
http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Maroc
http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Paris?nb=4
```

La réponse est un objet JSON avec exactement les propriétés
`ville_ou_pays`, `endroits_a_visiter`, `prix_moyen_repas`.
Le bonus utilise **un paramètre de requête `nb`**, avec 2 visites par défaut.
Les valeurs admises vont de 1 à 10. Le modèle est configuré avec
`ResponseFormat.JSON` et un message système demandant de ne pas utiliser Markdown.
Chaque requête est indépendante : aucun historique de conversation partagé.
La ressource vérifie le JSON et le nombre de visites avant de le renvoyer.

Erreurs : 400 pour un lieu vide ou `nb` hors limites, 503 si la configuration
Gemini manque, 502 si l’appel échoue ou si le JSON reçu est invalide.
Une valeur non numérique de `nb` est rejetée par JAX-RS avant l’appel de la méthode
(404 sur le Payara utilisé). Les détails du fournisseur ne sont pas renvoyés.

## Travail préparatoire

Les commits conservent le projet initial, l’exercice REST et le tableau JSON,
puis l’intégration Gemini, le bonus, les tests et la documentation.
Les dates des commits correspondent à la réalisation effective des étapes.

À l’étape préparatoire, l’URL
`http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Nice`
renvoie **`["Nice"]`** dans le navigateur.
JAX-RS transforme le tableau Java `String[]` en tableau JSON grâce à `@Produces`.
Cette version est conservée dans le commit `0ff0147` ; la version finale appelle Gemini.

`/api/hello-world` renvoie `Hello, World!`.
`/api/hello-world/personnes/Mohamed` renvoie volontairement une erreur 500,
comme demandé à la fin de l’introduction JAX-RS ; le retour 200 reste en commentaire.
La racine de l’application peut afficher 404 car elle ne contient pas de page d’accueil.

## Vérifications effectuées

- Compilation et création du WAR : succès.
- Deux tests Java : transmission du lieu/nombre et indépendance des requêtes.
- Payara 7.2026.11 : tableau préparatoire `["Nice"]`, erreur 500 préparatoire.
- Dix cas HTTP sur la version finale avec une API Gemini locale simulée : succès.
  Ils couvrent les 2 visites par défaut, le bonus à 4 visites, les paramètres
  invalides, le JSON invalide, le mauvais nombre de visites et l’erreur fournisseur.
- Sans clé Gemini : réponse 503 vérifiée séparément.

Un appel réel à **Gemini 3.5 Flash** a été vérifié le 15 novembre 2026 :
`GET /api/guide/lieu/Paris?nb=4` a retourné HTTP 200 et quatre visites.
La clé est conservée uniquement dans `.env` (permissions 600), exclu de Git.
Les données « Visite simulee » restent réservées aux tests automatisés locaux.
Le prix fourni par le modèle est une estimation.

Réponse réelle observée :

```json
{"ville_ou_pays":"Paris","endroits_a_visiter":["La Tour Eiffel","Le Musée du Louvre","La Cathédrale Notre-Dame de Paris","La Basilique du Sacré-Cœur"],"prix_moyen_repas":"20 €"}
```

Pour reproduire les tests HTTP sans envoyer de requête à Google :

1. Lancer `python3 tests/gemini_simule.py` dans un premier terminal.
2. Dans un deuxième terminal, définir :
   ```bash
   export GEMINI_API_KEY='cle-factice-test'
   export GEMINI_MODEL='modele-test'
   export GEMINI_API_ENDPOINT='http://127.0.0.1:18086/'
   ./lancer.sh
   ```
3. Dans un troisième terminal : `python3 tests/verifier_rest.py`.
4. Arrêter les serveurs avec Ctrl+C, puis supprimer `GEMINI_API_ENDPOINT`
   de l’environnement avant tout lancement avec une vraie clé.

## Éléments à mettre dans le mail de fin de TP

Le TP demande de mentionner dans le texte du mail :
- URL préparatoire : `http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Nice`.
- Affichage observé du tableau : `["Nice"]`.
- Solution bonus : paramètre de requête `nb`, 2 par défaut.
- URL bonus : `http://localhost:18085/tp3-rest-chafii/api/guide/lieu/Paris?nb=4`.
- Réponse réelle de Gemini : reproduite ci-dessus (HTTP 200, Paris, 4 visites).

Le dépôt GitHub est public : https://github.com/Camelchafii691/tp3-rest-chafii.
Aucun mail n’a été envoyé.
