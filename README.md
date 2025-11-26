# 🧩 Guildmaster : Suite de Modules Réutilisables

Le projet **Guildmaster** est le référentiel parent (Mono-Repo) qui centralise le développement, la configuration, et la gestion des versions de ma suite de modules réutilisables basés sur Spring Boot. Ces modules sont conçus pour offrir des fonctionnalités transversales (authentification, logging, etc.) dans une architecture modulaire et découplée.

---

## 🎯 Architecture et Versions

* **Version Actuelle :** `1.0.4`
* **Technologie :** Spring Boot 3 / Java 21
* **Type de Projet :** Maven Multi-Module (Packaging `pom`)

### Structure des Modules

Chaque module est découpé en deux parties principales :

1.  **`-core` :** Contient les interfaces, les APIs, les annotations, et la logique métier agnostique de l'infrastructure.
2.  **`-starter` :** Contient l'implémentation, l'auto-configuration Spring Boot, et les composants d'infrastructure (Controllers, Filters, Aspects) qui activent la fonctionnalité pour une installation rapide, clé en main.

---

## 📚 Modules Intégrés

Cliquez sur les liens pour accéder à la documentation détaillée de chaque module :

| Module | Rôle Principal | Lien vers la Documentation |
| :--- | :--- | :--- |
| **Sentinel** | Gestion de l'**Authentification** et de la **Sécurisation**. | [🔒 Module Sentinel](Sentinel/sentinel-core/README.md) |
| **Archivist** | Gestion centralisée de la **Journalisation** et de la **Supervision**. | [📜 Module Archivist](Archivist/archivist-core/README.md) |
| **Bard** | Gestion des **Traductions** et de l'**Internationalisation** (I18N). | [🌐 Module Bard](Bard/bard-core/README.md) |

---

## 🚀 Utilisation et Installation

Pour intégrer un module dans votre projet, vous devez inclure sa dépendance `-starter` dans votre `pom.xml`.

⚠️ Propriétés du Compilateur Obligatoires

Puisque toute la suite Guildmaster est compilée et exécutée en Java 21, il est impératif de définir la version de Java dans le bloc <properties> de votre projet de consommation pour garantir la compatibilité et éviter l'erreur UnsupportedClassVersionError :

```XML
<properties>
    <java.version>21</java.version>
    
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

### Exemple : Intégration de Sentinel (Sécurité)

```XML
<dependency>
    <groupId>guildmaster</groupId>
    <artifactId>sentinel-starter</artifactId>
    <version>1.0.3</version>
</dependency>
```
