# WalletZen no Kubernetes — `config-server`

Ambiente de **estudo**, paralelo ao `docker-compose.yml`: sobe **apenas o `config-server`**
num cluster local, sem encostar no resto da stack. O Compose continua sendo a forma oficial
de rodar o WalletZen inteiro (veja o [README principal](../README.md)).

O `config-server` foi escolhido por ser o único serviço sem dependências: não tem banco,
não fala com Kafka, não se registra no Eureka e não valida JWT. No profile `native`, o
`config-repo/` já vem embutido na imagem, então ele sobe sozinho — sem token do GitHub.

## Pré-requisitos

- **Docker Desktop** com Kubernetes habilitado: *Settings → Kubernetes → Enable Kubernetes*.
- **`kubectl`** — já vem com o Docker Desktop, não precisa instalar.
- A imagem `walletzen/config-server:latest` presente na máquina. Se não estiver:
  ```bash
  docker compose -f ../docker-compose.yml build config-server
  ```
  O cluster do Docker Desktop compartilha o armazenamento de imagens do Docker, então não
  há nada a "carregar" depois do build.

## Subir

```bash
kubectl config use-context docker-desktop
```

```bash
kubectl apply -f .
```

```bash
kubectl config set-context --current --namespace=walletzen
```

```bash
kubectl get pods -w
```

Aguarde o `READY` sair de `0/1` e chegar em `1/1` (uns 20 a 40 segundos — é a `startupProbe`
esperando o Spring Boot terminar de subir).

## Verificar

```bash
kubectl get all
```

```bash
kubectl get endpoints config-server
```

O `ENDPOINTS` deve listar o IP do pod com a porta (`10.1.0.x:8888`). **Endpoint vazio
significa que o `selector` do Service não casou com as labels do pod** — é o primeiro lugar
a olhar quando "o Service não funciona".

```bash
kubectl logs deploy/config-server --tail=50
```

Procure por `The following 1 profile is active: "native"` e `Tomcat started on port 8888`.

## Acessar

O Service é `NodePort`, então sai direto em `localhost`, sem túnel:

```bash
curl.exe -s http://localhost:30888/actuator/health
```

```bash
curl.exe -s http://localhost:30888/wz-financial/default
```

A segunda resposta traz as propriedades do `wz-financial` com
`"walletzen.config-source":"config-server (native / config-repo)"` — a prova de que veio do
`config-repo/` embutido na imagem.

> No PowerShell use **`curl.exe`**, com extensão: `curl` puro é apelido de
> `Invoke-WebRequest` e tem outra sintaxe.

Alternativa sem `NodePort` (túnel que prende o terminal e cai quando o pod é trocado):

```bash
kubectl port-forward svc/config-server 8888:8888
```

## Os arquivos

| Arquivo | Objeto | Papel |
| ------- | ------ | ----- |
| `00-namespace.yaml` | `Namespace` | isola os objetos do projeto dos do `kube-system` |
| `10-configmap.yaml` | `ConfigMap` | variáveis de ambiente: profile `native` e opções da JVM |
| `20-deployment.yaml` | `Deployment` | imagem, réplicas, probes e limites de recurso |
| `30-service.yaml` | `Service` | nome estável + `NodePort` 30888 |

Os números no nome não significam nada para o Kubernetes — servem só para o
`kubectl apply -f .` processar na ordem certa (o namespace precisa existir antes do resto).

## Três detalhes que evitam dor de cabeça

**`imagePullPolicy: IfNotPresent` é obrigatório.** Com a tag `latest`, o padrão do Kubernetes
é `Always`: ele tentaria baixar `walletzen/config-server` do Docker Hub (onde não existe) e o
pod ficaria em `ErrImagePull`, mesmo com a imagem ali na máquina.

**`JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=75`** (no `ConfigMap`) faz a JVM dimensionar o heap
pela memória **do container**. Sem isso ela olha a memória da máquina inteira, estoura o
`limits.memory` e o pod morre com `OOMKilled` — sem nenhuma exceção no log.

**Liveness e readiness apontam para endpoints diferentes, de propósito.** A `livenessProbe`
usa `/actuator/health/liveness` (só o estado da aplicação); a `readinessProbe` usa
`/actuator/health` (que inclui o indicador do Config Server). Assim, se o backend de
configuração ficar indisponível, o pod sai do Service mas **não** é morto e recriado em loop
por um problema externo.

## Experimentos

```bash
kubectl delete pod -l app=config-server      # o ReplicaSet recria sozinho
```

```bash
kubectl scale deploy/config-server --replicas=2   # dois endpoints no Service
```

```bash
kubectl rollout restart deploy/config-server      # troca os pods sem downtime
```

```bash
kubectl rollout undo deploy/config-server         # volta para o ReplicaSet anterior
```

```bash
kubectl describe pod <nome>                        # onde o cluster conta o que fez e por quê
```

> **Atenção ao drift:** `kubectl scale`, `edit` e `patch` mudam o cluster, **não** os arquivos.
> Depois de experimentar, `kubectl apply -f .` devolve o cluster ao que está versionado aqui.

## Limpar

```bash
kubectl delete -f .
```

Isso remove os objetos; o cluster continua de pé. Para desligar o cluster e recuperar o ~1 GB
de RAM, desmarque *Enable Kubernetes* no Docker Desktop — especialmente antes de subir a
stack do Compose, que consome cerca de 3,3 GB.

Se o cluster ficar num estado estranho, *Settings → Kubernetes → Reset Kubernetes Cluster*
devolve tudo ao zero sem reinstalar nada.

## Fora de escopo

Só o `config-server` mora aqui. Os demais serviços dependem de Postgres, Kafka, Eureka e
Keycloak, e levá-los para o cluster exige decidir o que fica dentro e o que fica fora — outro
exercício, bem maior. Também não há `Ingress`, Helm, Kustomize nem CI aplicando manifestos.
