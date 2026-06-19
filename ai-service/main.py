import os
import random
from urllib.parse import quote
from typing import Optional

import requests
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="Jiangmai AI Service")

class ChatRequest(BaseModel):
    message: str
    user_id: Optional[int] = None

class PromptRequest(BaseModel):
    prompt: str
    user_id: Optional[int] = None

class ImageGenerateRequest(BaseModel):
    prompt: str
    style: Optional[str] = "realistic"
    size: Optional[str] = "1024x1024"

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "").strip()
DEEPSEEK_API_URL = os.getenv("DEEPSEEK_API_URL", "https://api.deepseek.com/v1/chat/completions")
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "").strip()
OPENAI_API_URL = os.getenv("OPENAI_API_URL", "https://api.openai.com/v1/chat/completions")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
ARK_API_KEY = os.getenv("ARK_API_KEY", "").strip()
DOUBAO_API_URL = os.getenv("DOUBAO_API_URL", "https://ark.cn-beijing.volces.com/api/v3/responses")
DOUBAO_MODEL = os.getenv("DOUBAO_MODEL", "doubao-seed-1-6-250615")
AI_PROVIDER = os.getenv("AI_PROVIDER", "openai").strip().lower()
IMAGE_PROVIDER = os.getenv("IMAGE_PROVIDER", "auto").strip().lower()
OPENAI_IMAGE_API_URL = os.getenv("OPENAI_IMAGE_API_URL", "https://api.openai.com/v1/images/generations")
OPENAI_IMAGE_MODEL = os.getenv("OPENAI_IMAGE_MODEL", "gpt-image-1")
POLLINATIONS_IMAGE_BASE_URL = os.getenv("POLLINATIONS_IMAGE_BASE_URL", "https://image.pollinations.ai/prompt")
DOUBAO_IMAGE_API_URL = os.getenv("DOUBAO_IMAGE_API_URL", "https://ark.cn-beijing.volces.com/api/v3/images/generations")
DOUBAO_IMAGE_MODEL = os.getenv("DOUBAO_IMAGE_MODEL", "doubao-seedream-5-0-260128")

SYSTEM_PROMPT = (
    "你是一位资深的非遗锡雕专家与辅学老师，"
    "负责为平台学员解答关于锡雕历史、工艺技术、工具使用和创作实践的问题。"
    "如果用户询问无关话题，请礼貌地引导回锡雕或手工艺领域。"
)

@app.get("/api/ai/health")
def health_check():
    provider = resolve_provider()
    return {
        "status": "ok",
        "service": "AI Service is running",
        "provider": provider,
        "provider_connected": has_provider_key(provider)
    }

def resolve_provider() -> str:
    if AI_PROVIDER in ("openai", "gpt"):
        return "openai"
    if AI_PROVIDER == "deepseek":
        return "deepseek"
    if AI_PROVIDER == "doubao":
        return "doubao"
    if ARK_API_KEY:
        return "doubao"
    if OPENAI_API_KEY:
        return "openai"
    if DEEPSEEK_API_KEY:
        return "deepseek"
    return "openai"

def has_provider_key(provider: str) -> bool:
    if provider == "openai":
        return bool(OPENAI_API_KEY)
    if provider == "deepseek":
        return bool(DEEPSEEK_API_KEY)
    if provider == "doubao":
        return bool(ARK_API_KEY)
    return False

def call_provider(user_message: str) -> str:
    provider = resolve_provider()
    if provider == "openai":
        return call_chat_completion(
            api_key=OPENAI_API_KEY,
            api_url=OPENAI_API_URL,
            model=OPENAI_MODEL,
            missing_key_message="AI 服务未配置 OPENAI_API_KEY，请联系管理员配置。",
            user_message=user_message
        )
    if provider == "deepseek":
        return call_chat_completion(
            api_key=DEEPSEEK_API_KEY,
            api_url=DEEPSEEK_API_URL,
            model=DEEPSEEK_MODEL,
            missing_key_message="AI 服务未配置 DEEPSEEK_API_KEY，请联系管理员配置。",
            user_message=user_message
        )
    if provider == "doubao":
        return call_doubao(
            api_key=ARK_API_KEY,
            api_url=DOUBAO_API_URL,
            model=DOUBAO_MODEL,
            missing_key_message="AI 服务未配置 ARK_API_KEY，请联系管理员配置。",
            user_message=user_message
        )
    return f"不支持的 AI_PROVIDER: {provider}"

def call_doubao(
    api_key: str,
    api_url: str,
    model: str,
    missing_key_message: str,
    user_message: str
) -> str:
    if not api_key:
        return missing_key_message
    # Prefer Ark responses API; fallback to chat/completions if URL configured that way.
    if "/responses" in api_url:
        return call_doubao_responses(api_key, api_url, model, user_message)
    return call_chat_completion(api_key, api_url, model, missing_key_message, user_message)

def call_doubao_responses(api_key: str, api_url: str, model: str, user_message: str) -> str:
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": model,
        "input": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": f"{SYSTEM_PROMPT}\n\n用户问题：{user_message}"
                    }
                ]
            }
        ]
    }
    try:
        resp = requests.post(api_url, headers=headers, json=payload, timeout=60)
        resp.raise_for_status()
        data = resp.json()
        for item in data.get("output", []):
            if item.get("type") == "message":
                for c in item.get("content", []):
                    if c.get("type") == "output_text" and c.get("text"):
                        return c["text"]
        return "模型暂无返回，请稍后重试。"
    except requests.HTTPError as e:
        return f"AI 调用失败（HTTP {e.response.status_code}），请检查豆包模型或权限配置。"
    except Exception:
        return "AI 服务暂时不可用，请稍后重试。"

def resolve_image_provider() -> str:
    if IMAGE_PROVIDER in ("openai", "pollinations", "doubao"):
        return IMAGE_PROVIDER
    if ARK_API_KEY:
        return "doubao"
    if OPENAI_API_KEY:
        return "openai"
    return "pollinations"

def normalize_image_size(size: str) -> str:
    size = (size or "1024x1024").strip().lower()
    if size in ("1024x1024", "1536x1024", "1024x1536"):
        return size
    if size == "1920x1080":
        return "1536x1024"
    return "1024x1024"

def normalize_doubao_size(size: str) -> str:
    """
    豆包文生图要求最小像素约 3686400，因此将前端常用尺寸映射到更大尺寸。
    """
    size = (size or "").strip().lower()
    if size == "1920x1080":
        return "2560x1440"
    return "2048x2048"

def parse_size_to_wh(size: str) -> tuple[int, int]:
    safe = normalize_image_size(size)
    parts = safe.split("x")
    if len(parts) != 2:
        return (1024, 1024)
    try:
        return (int(parts[0]), int(parts[1]))
    except Exception:
        return (1024, 1024)

def build_image_prompt(prompt: str, style: str) -> str:
    style_map = {
        "realistic": "传统写实风格，注重金属质感与工艺细节。",
        "modern": "现代几何风格，构图简洁，线条清晰。",
        "ink": "水墨意境风格，留白与东方审美。"
    }
    style_hint = style_map.get((style or "").strip().lower(), "")
    return (
        "请生成非遗锡雕设计参考图。"
        f"{style_hint}\n"
        f"主题描述：{prompt}\n"
        "要求：画面完整、主体清晰、适合课程展示。"
    )

def build_compact_image_prompt(prompt: str, style: str) -> str:
    style_map = {
        "realistic": "传统写实",
        "modern": "现代几何",
        "ink": "水墨意境"
    }
    style_hint = style_map.get((style or "").strip().lower(), "传统写实")
    plain = " ".join((prompt or "").split())
    # 控制 prompt 长度，降低第三方图床 500/429 概率
    if len(plain) > 110:
        plain = plain[:110]
    return f"非遗锡雕设计图，{style_hint}风格，主题：{plain}，高清，主体清晰"

def is_image_url_available(url: str) -> bool:
    try:
        resp = requests.get(url, timeout=20, stream=True)
        ok = resp.status_code < 400 and str(resp.headers.get("Content-Type", "")).startswith("image/")
        resp.close()
        return ok
    except Exception:
        return False

def generate_image_with_openai(prompt: str, style: str, size: str) -> dict:
    if not OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY 未配置")
    headers = {
        "Authorization": f"Bearer {OPENAI_API_KEY}",
        "Content-Type": "application/json"
    }
    used_size = normalize_image_size(size)
    payload = {
        "model": OPENAI_IMAGE_MODEL,
        "prompt": build_image_prompt(prompt, style),
        "size": used_size
    }
    resp = requests.post(OPENAI_IMAGE_API_URL, headers=headers, json=payload, timeout=120)
    resp.raise_for_status()
    data = resp.json()
    first = (data.get("data") or [{}])[0]
    image_url = first.get("url")
    if image_url:
        return {"image_url": image_url, "provider": "openai", "size": used_size}
    b64_data = first.get("b64_json")
    if b64_data:
        return {
            "image_url": f"data:image/png;base64,{b64_data}",
            "provider": "openai",
            "size": used_size
        }
    raise RuntimeError("OpenAI 未返回可用图片")

def generate_image_with_doubao(prompt: str, style: str, size: str) -> dict:
    if not ARK_API_KEY:
        raise ValueError("ARK_API_KEY 未配置")
    headers = {
        "Authorization": f"Bearer {ARK_API_KEY}",
        "Content-Type": "application/json"
    }
    used_size = normalize_doubao_size(size)
    payload = {
        "model": DOUBAO_IMAGE_MODEL,
        "prompt": build_image_prompt(prompt, style),
        "size": used_size
    }
    resp = requests.post(DOUBAO_IMAGE_API_URL, headers=headers, json=payload, timeout=120)
    resp.raise_for_status()
    data = resp.json()
    first = (data.get("data") or [{}])[0]
    image_url = first.get("url")
    if not image_url:
        raise RuntimeError("豆包未返回可用图片")
    return {"image_url": image_url, "provider": "doubao", "size": used_size}

def generate_image_with_pollinations(prompt: str, style: str, size: str) -> dict:
    used_size = normalize_image_size(size)
    width, height = parse_size_to_wh(used_size)
    compact_prompt = build_compact_image_prompt(prompt, style)
    candidates = []
    for _ in range(3):
        seed = random.randint(1, 999999999)
        candidates.append(
            f"{POLLINATIONS_IMAGE_BASE_URL}/{quote(compact_prompt)}"
            f"?width={width}&height={height}&seed={seed}&nologo=true"
        )
        candidates.append(
            f"{POLLINATIONS_IMAGE_BASE_URL}/{quote(compact_prompt)}"
            f"?width={width}&height={height}&seed={seed}&model=flux&nologo=true"
        )
    for image_url in candidates:
        if is_image_url_available(image_url):
            return {"image_url": image_url, "provider": "pollinations", "size": used_size}
    raise RuntimeError("图片服务暂时繁忙，请稍后重试")

def call_chat_completion(
    api_key: str,
    api_url: str,
    model: str,
    missing_key_message: str,
    user_message: str
) -> str:
    if not api_key:
        return missing_key_message

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": model,
        "temperature": 0.7,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_message}
        ]
    }

    try:
        resp = requests.post(api_url, headers=headers, json=payload, timeout=60)
        resp.raise_for_status()
        data = resp.json()
        choices = data.get("choices", [])
        if not choices:
            return "模型暂无返回，请稍后重试。"
        return choices[0].get("message", {}).get("content", "模型返回为空。")
    except requests.HTTPError as e:
        return f"AI 调用失败（HTTP {e.response.status_code}），请检查 API Key 或模型配置。"
    except Exception:
        return "AI 服务暂时不可用，请稍后重试。"

@app.post("/api/ai/chat")
def ai_chat(request: ChatRequest):
    return {
        "reply": call_provider(request.message),
        "user_id": request.user_id or 0
    }

@app.post("/api/ai/review")
def ai_review(request: PromptRequest):
    review_prompt = (
        "请从锡雕创作角度对以下内容给出专业点评，"
        "包含：优点、可改进点、具体建议。\n\n"
        f"待点评内容：{request.prompt}"
    )
    return {"reply": call_provider(review_prompt), "user_id": request.user_id}

@app.post("/api/ai/generate")
def ai_generate(request: PromptRequest):
    generate_prompt = (
        "请基于以下需求生成一份锡雕创作方案，"
        "包含：主题、材料、工艺步骤、注意事项、成品呈现建议。\n\n"
        f"需求：{request.prompt}"
    )
    return {"reply": call_provider(generate_prompt), "user_id": request.user_id}

@app.post("/api/ai/image-generate")
def ai_image_generate(request: ImageGenerateRequest):
    if not request.prompt or not request.prompt.strip():
        return {"error": "prompt 不能为空"}
    preferred = resolve_image_provider()
    providers = [preferred]
    for p in ("doubao", "openai", "pollinations"):
        if p not in providers:
            providers.append(p)
    last_error = "图片生成失败，请稍后重试"
    for p in providers:
        try:
            if p == "doubao":
                return generate_image_with_doubao(request.prompt, request.style or "", request.size or "")
            if p == "openai":
                return generate_image_with_openai(request.prompt, request.style or "", request.size or "")
            return generate_image_with_pollinations(request.prompt, request.style or "", request.size or "")
        except Exception as e:
            last_error = str(e) or last_error
    return {"error": last_error}
