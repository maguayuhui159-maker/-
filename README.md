# Jiangmai Platform Deployment

## 1. Prepare `.env`
Copy `.env.example` to `.env` and set your real API key:

```bash
AI_PROVIDER=openai
OPENAI_API_KEY=your_real_openai_api_key
OPENAI_MODEL=gpt-4o-mini
```

If you want to use DeepSeek, set:
```bash
AI_PROVIDER=deepseek
DEEPSEEK_API_KEY=your_real_deepseek_api_key
```

If you want to use Doubao (Volcengine Ark), set:
```bash
AI_PROVIDER=doubao
ARK_API_KEY=your_ark_api_key
DOUBAO_MODEL=doubao-seed-1-6-250615
```

For image generation in AI page:
```bash
IMAGE_PROVIDER=auto
OPENAI_IMAGE_MODEL=gpt-image-1
```
`auto` means: try OpenAI first, fallback to online image generation service automatically.

## 2. One-click deploy

### Windows (PowerShell)
```powershell
.\deploy.ps1
```

### Linux / macOS
```bash
chmod +x deploy.sh
./deploy.sh
```

## 3. Access URLs
- Admin Web: `http://localhost/`
- Backend API health: `http://localhost/api/health`
- AI Service health: `http://localhost/api/ai/health`

## 4. AI quick test
```bash
curl -X POST "http://localhost/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"请介绍一下锡雕入门步骤\",\"user_id\":1}"
```
