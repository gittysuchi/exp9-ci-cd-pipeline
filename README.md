# Experiment 9 — CI/CD Pipeline with GitHub Actions

**Aim:** design and implement a **Continuous Integration** pipeline (automated build + unit / API / contract / Testcontainers tests) and a **Continuous Deployment** pipeline (Docker + GitHub Actions + canary release) for a Spring Boot application.

> 📄 The ready-to-paste lab report is in **`docs/Experiment9_Report.html`** — open it in a browser, press `Ctrl + A`, `Ctrl + C`, and paste into Word / your record. Fill in the yellow fields (name, roll no., …).

## CI vs CD in one look

| | CI — `.github/workflows/ci.yml` | CD — `.github/workflows/cd.yml` |
|---|---|---|
| Question | "Does the new code build and pass all tests?" | "Can this verified build be released safely?" |
| Trigger | every push / pull request | automatically after CI succeeds on `main` |
| Output | tested JAR + reports | released, running version |
| On failure | code rejected, nothing deployed | canary rolled back, users keep the old version |

## Jobs

**CI** — `CI - Build and Test Pipeline`

| # | Job name | What it does |
|---|---|---|
| 1 | Build and Compile | compile main + test code on JDK 17 |
| 2 | Unit Tests (JUnit 5 + Mockito) | `ProductServiceTest` |
| 3 | API and Integration Tests (MockMvc) | `ProductControllerTest`, `ProductApiIntegrationTest` |
| 4 | Infrastructure Tests (Testcontainers - PostgreSQL) | `ProductPostgresContainerTest` against a real PostgreSQL container |
| 5 | Contract Tests (Pact - frontend vs backend) | `ProductConsumerPactTest` → pact file → `ProductProviderPactTest` |
| 6 | Package JAR and Code Coverage | `mvn verify`, uploads JAR + JaCoCo report |
| 7 | CI Gate - all checks passed | one pass/fail result for the run |

**CD** — `CD - Docker Build, Push and Canary Release`

| # | Job name | What it does |
|---|---|---|
| 1 | Docker Build and Push Image | build `Dockerfile`, push to GitHub Container Registry (Docker Hub optional) |
| 2 | Canary Deploy (10 percent traffic) and Verify | nginx sends 10 % of traffic to the new version; script checks for errors |
| 3 | Promote to 100 percent (Full Rollout) | full rollout, image tagged `stable` + `latest` |
| 4 | Rollback (keep previous stable release) | runs only if the canary failed; `stable` tag is never moved |

## Put it in VS Code

1. Extract the zip → **File → Open Folder** → pick `exp9-ci-cd-pipeline`.
2. VS Code suggests the recommended extensions (Java, Spring Boot, Docker, GitHub Actions) — install them.
3. Install **JDK 17+** and **Maven** (`winget install Apache.Maven`), and **Docker Desktop** (needed only for Testcontainers and the Docker steps).

## Run locally (optional)

```bash
mvn clean verify                      # runs ALL tests (Testcontainers tests are skipped if Docker isn't running)
mvn spring-boot:run                   # app on http://localhost:8080
docker build -t exp9-cicd-demo:local .
docker run --rm -p 8080:8080 exp9-cicd-demo:local
```

Handy tasks are in **Terminal → Run Task…** (`.vscode/tasks.json`).

> The Pact contract file `src/test/resources/pacts/product-frontend-product-service.json` is **generated** by the consumer test the first time you run the tests. Run the whole suite (or the consumer test first) — the provider test reads that file. Commit the generated file.

## Push to GitHub from the VS Code terminal

1. On github.com → **New repository** → name `exp9-ci-cd-pipeline` → leave "Add a README" **unchecked** → **Create**.
2. VS Code → open the terminal (`` Ctrl + ` ``) and run:

```bash
git config --global user.name  "Your Name"
git config --global user.email "your-email@example.com"

git init -b main
git add .
git commit -m "Experiment 9: CI/CD pipeline"
git remote add origin https://github.com/<your-username>/exp9-ci-cd-pipeline.git
git push -u origin main
```

   (A browser window opens for GitHub sign-in the first time. If a password is requested in the terminal, use a **Personal Access Token**.)

**One-command version** (does init + add + commit + remote + push):

```powershell
.\scripts\push-to-github.ps1 -RepoUrl https://github.com/<your-username>/exp9-ci-cd-pipeline.git     # PowerShell
bash scripts/push-to-github.sh https://github.com/<your-username>/exp9-ci-cd-pipeline.git            # Git Bash / WSL / Mac / Linux
```

**Automatic posting** — after this one-time command, every `git commit` is pushed to GitHub by itself and starts the CI pipeline:

```bash
git config core.hooksPath .githooks
```

3. Open your repo → **Actions** tab. CI starts on the push; when it succeeds, CD starts automatically.

### Optional GitHub settings

* **Docker Hub mirror:** Settings → Secrets and variables → Actions → add `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`. Without them the image goes only to GitHub Container Registry (`ghcr.io`), which needs no setup.
* **Manual approval before 100 % rollout:** Settings → Environments → `production` → *Required reviewers*.
* **Demo the rollback:** Actions → *CD …* → **Run workflow** → tick `simulate_canary_failure`. The canary returns HTTP 500, verification fails, and the *Rollback* job runs.

## How the canary release works

```
client ──► nginx gateway ──90%──► stable  (previous release, image tag :stable)
                        └─10%──► canary  (new release, image tag :sha-xxxxxxx)
```

`deploy/verify-canary.sh` sends 50 requests, counts responses per release via the `X-App-Track` header, and **passes only if the canary received traffic and produced zero errors**. Pass → `deploy/full-rollout.sh` moves 100 % of traffic to the new version and the image is tagged `stable`/`latest`. Fail → rollback (the `stable` tag never moves).

The same `deploy/` files run on any server with Docker. On GitHub's runner they run in a temporary environment, which is what demonstrates the strategy for this experiment.

## Project layout

```
.github/workflows/ci.yml        CI workflow (7 jobs)
.github/workflows/cd.yml        CD workflow (4 jobs)
src/main/java/...               Product Catalogue REST service (+ ProductClient = "frontend" consumer)
src/main/resources/static/      web page that shows which release answered
src/test/java/...               unit · api · containers · contract tests
deploy/                         docker-compose.canary.yml, nginx configs, canary scripts
scripts/                        push-to-github.ps1/.sh, test-summary.sh
.githooks/post-commit           auto-push hook
Dockerfile                      multi-stage build, non-root, health check
docs/Experiment9_Report.html    lab report (paste into your record)
```

## Troubleshooting

| Problem | Fix |
|---|---|
| `git push` asks for a password | use a Personal Access Token, or sign in through the browser prompt |
| CD never starts | CD starts only after the **CI workflow succeeds on `main`**; check the Actions tab for a red CI run |
| Image push fails with *permission denied* | Settings → Actions → General → Workflow permissions → **Read and write permissions** |
| Testcontainers tests show as *skipped* locally | start Docker Desktop (on GitHub Actions they always run) |
| `mvn` not recognised | install Maven and reopen the terminal |
