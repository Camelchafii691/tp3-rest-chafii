# IA.md — Utilisation de l'intelligence artificielle

**Date de la conversation :** 15 novembre 2026  
**Assistant utilisé :** ChatGPT (OpenAI, GPT-6)  
**Sujet :** Intégration de Gemini avec LangChain4j dans une API REST Java (Maven, CDI, JAX-RS et CORS)

## 1. Objectif

Ce fichier documente les échanges avec l'assistant IA afin de comprendre et de préparer :

- l'intégration du modèle Gemini via LangChain4j dans un projet Maven ;
- la gestion sécurisée de la clé API par variable d'environnement ;
- l'injection d'un client LLM dans une ressource REST avec CDI ;
- l'initialisation d'un bean avec `@PostConstruct` ;
- la gestion de CORS dans une API JAX-RS.

Les extraits ci-dessous sont des **exemples proposés durant la conversation** : ils ne constituent pas une preuve d'exécution, de compilation ou de déploiement.

## 2. Prompt n° 1 — Dépendances Maven et sécurité de la clé API

**Prompt envoyé :**

> Quelles dépendances Maven faut-il pour utiliser Gemini avec LangChain4j, et comment lire ma clé API depuis une variable d’environnement sans la mettre sur GitHub ?

**Réponse et aide apportées par l'IA :**

- Ajouter la bibliothèque principale LangChain4j et son intégration Google AI Gemini dans `pom.xml`.
- Créer la variable d'environnement `GEMINI_API_KEY` et la lire avec `System.getenv`.
- Éviter toute clé API codée en dur ou publiée dans un dépôt.
- Utiliser `.gitignore` pour exclure notamment les fichiers `.env`.

### Exemple de dépendances Maven

```xml
<dependencies>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>1.23.0</version>
    </dependency>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-google-ai-gemini</artifactId>
        <version>1.23.0</version>
    </dependency>
</dependencies>
```

> **Remarque :** les versions indiquées reproduisent l'exemple fourni dans la conversation. Il convient de vérifier leur disponibilité et leur compatibilité dans Maven Central et la documentation LangChain4j avant utilisation.

### Lecture de la variable d'environnement

```java
String apiKey = System.getenv("GEMINI_API_KEY");
if (apiKey == null || apiKey.isBlank()) {
    throw new IllegalStateException("GEMINI_API_KEY manquante");
}
```

**Définition temporaire sur Windows (PowerShell) :**

```powershell
$env:GEMINI_API_KEY="VOTRE_CLE_API"
```

**Définition temporaire sur macOS / Linux :**

```bash
export GEMINI_API_KEY="VOTRE_CLE_API"
```

**Exemple de `.gitignore` :**

```gitignore
.env
.env.*
!.env.example
secrets.properties
*.key
target/
```

**Précautions :**

- `System.getenv()` ne lit pas automatiquement le contenu d'un fichier `.env` : il faut charger la variable dans le processus Java ou utiliser un outil adapté.
- Si une clé a déjà été envoyée sur GitHub, l'ajout de `.gitignore` ne suffit pas. Il faut révoquer la clé exposée et en créer une autre.

## 3. Prompt n° 2 — Injection CDI et cycle de vie du client LLM

**Prompt envoyé :**

> Comment injecter ma classe client LLM dans ma ressource REST avec `@Inject` ? Quel scope CDI choisir, et à quoi sert `@PostConstruct` ?

**Réponse et aide apportées par l'IA :**

- Utiliser `@ApplicationScoped` sur le client Gemini pour réutiliser la même configuration entre les requêtes.
- Utiliser `@Inject` dans la ressource REST pour obtenir ce client sans l'instancier manuellement.
- Utiliser `@PostConstruct` pour initialiser le modèle après la construction du bean et l'injection de ses dépendances.
- Veiller à ce que le serveur ou le framework prenne en charge CDI et Jakarta REST.

### Exemple de client LLM CDI

```java
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@ApplicationScoped
public class GeminiClient {
    private ChatModel model;

    @PostConstruct
    public void init() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY manquante");
        }
        model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .build();
    }

    public String demander(String question) {
        return model.chat(question);
    }
}
```

### Exemple de ressource REST avec injection

```java
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/gemini")
public class GeminiResource {
    @Inject
    GeminiClient geminiClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String poserQuestion(@QueryParam("question") String question) {
        if (question == null || question.isBlank()) {
            return "Veuillez fournir une question.";
        }
        return geminiClient.demander(question);
    }
}
```

### Explication des annotations

| Annotation | Utilité |
|---|---|
| `@Inject` | Demander à CDI de fournir la dépendance. |
| `@ApplicationScoped` | Partager le bean à l'échelle de l'application. |
| `@RequestScoped` | Créer un contexte propre à chaque requête. |
| `@PostConstruct` | Initialiser un bean une fois par instance, après injection. |

**Point d'attention :** un client partagé peut être utilisé par plusieurs requêtes simultanées. Ne pas stocker des données propres à un utilisateur dans ses champs partagés.

## 4. Prompt n° 3 — CORS et `Response.ResponseBuilder` dans JAX-RS

**Prompt envoyé :**

> Qu’est-ce que CORS, et comment ajouter le header `Access-Control-Allow-Origin` à une réponse JAX-RS avec `Response.ResponseBuilder` ?

**Réponse et aide apportées par l'IA :**

- CORS (*Cross-Origin Resource Sharing*) est un mécanisme appliqué par les navigateurs pour contrôler l'accès à des réponses HTTP entre origines différentes.
- Un frontend à `http://localhost:3000` et un backend à `http://localhost:8080` ont des origines différentes.
- La réponse JAX-RS peut déclarer l'origine autorisée avec `.header("Access-Control-Allow-Origin", "http://localhost:3000")`.
- Certaines requêtes nécessitent un échange préalable `OPTIONS` (*preflight*).

### Exemple de réponse REST avec header CORS

```java
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/gemini")
public class GeminiResource {
    @Inject
    GeminiClient geminiClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response poserQuestion(@QueryParam("question") String question) {
        if (question == null || question.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Question manquante")
                    .header("Access-Control-Allow-Origin", "http://localhost:3000")
                    .build();
        }

        String reponse = geminiClient.demander(question);

        return Response.ok(reponse)
                .header("Access-Control-Allow-Origin", "http://localhost:3000")
                .build();
    }
}
```

### Exemple simplifié de traitement OPTIONS

```java
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.core.Response;

@OPTIONS
public Response options() {
    return Response.noContent()
            .header("Access-Control-Allow-Origin", "http://localhost:3000")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
            .build();
}
```

### Explication de `ResponseBuilder`

| Méthode | Rôle |
|---|---|
| `Response.ok(reponse)` | Préparer une réponse HTTP de statut 200. |
| `Response.status(...)` | Définir un autre statut HTTP. |
| `.entity(...)` | Définir le corps de la réponse. |
| `.header(nom, valeur)` | Ajouter un header HTTP. |
| `.build()` | Construire l'objet `Response`. |

**Bonnes pratiques :**

- Préférer l'origine explicite du frontend à `*` lorsque l'API n'est pas publique.
- `Access-Control-Allow-Origin: *` ne convient pas aux requêtes CORS avec identifiants.
- Pour une application réelle, centraliser CORS dans un `ContainerResponseFilter` ou une configuration du framework ; gérer aussi les réponses aux demandes de preflight.
- Préférer une route `POST` avec un corps JSON pour envoyer des questions susceptibles de contenir des données sensibles plutôt qu'un paramètre d'URL.

## 5. Synthèse des acquis

À l'issue de cette conversation, les notions suivantes ont été expliquées :

1. **Maven / LangChain4j :** ajouter les dépendances nécessaires pour utiliser Gemini depuis Java.
2. **Secrets :** utiliser une variable d'environnement pour éviter d'inscrire la clé API dans le code ou Git.
3. **CDI :** séparer le client LLM de la ressource REST et les relier par `@Inject`.
4. **Cycle de vie :** utiliser `@ApplicationScoped` pour le client partagé et `@PostConstruct` pour son initialisation.
5. **JAX-RS :** produire des réponses HTTP avec `Response.ResponseBuilder`.
6. **CORS :** autoriser explicitement le frontend et prendre en charge les requêtes `OPTIONS` lorsque nécessaire.

## 6. Limites et vérifications à effectuer

Le contenu de cette conversation a servi de **guide de conception et d'apprentissage**. Il n'y a pas, dans les messages échangés, de preuve que les dépendances ont été téléchargées, que les classes ont été compilées, que l'API a été démarrée, ni que les appels Gemini et CORS ont été testés.

Avant de remettre ou déployer le projet, vérifier les versions Maven, la disponibilité du modèle Gemini choisi, la configuration CDI/JAX-RS du framework, la présence effective de `GEMINI_API_KEY` dans l'environnement du processus Java, et les réponses CORS (notamment OPTIONS) depuis un navigateur.

---

**Note sur l'utilisation de l'IA :** l'assistant a fourni des explications, des exemples de configuration et des extraits de code. La validation et l'intégration effectives de ces éléments dans le projet restent à réaliser par le développeur.
