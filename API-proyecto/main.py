from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from rutas import router as rutas

app = FastAPI()

# Configuración de CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5500",
        "http://192.168.165.3:8000",
        "http://192.168.165.209:8000",
        "http://192.168.165.244:8000",
        "http://localhost:8000",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Registro de rutas
app.include_router(rutas)

@app.get("/")
async def read_root():
    return {"message": "API en funcionamiento"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
