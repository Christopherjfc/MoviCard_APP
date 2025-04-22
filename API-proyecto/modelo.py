from pydantic import BaseModel
from datetime import date
from enum import Enum
from typing import Optional


class PlanEnum(str, Enum):
    GRATUITA = "GRATUITA"
    PREMIUM = "PREMIUM"

class EstadoEnum(str, Enum):
    ACTIVA = "ACTIVA"
    BLOQUEADA = "BLOQUEADA"

class TipoTransporteEnum(str, Enum):
    METRO = "METRO"
    RENFE = "RENFE"
    
class TicketTipo(str, Enum):
    TENMOVI = "TENMOVI"
    MOVIMES = "MOVIMES"
    TRIMOVI = "TRIMOVI"

class Cliente(BaseModel):
    id: int
    nombre: str
    apellido: str
    dni: str
    correo: str
    telefono: str
    direccion: str
    numero_bloque: str
    numero_piso: str | None
    codigopostal: str
    ciudad: str
    password: str

class Suscripcion(BaseModel):
    id: int
    suscripcion: PlanEnum
    id_cliente: int
    

class Ticket(BaseModel):
    id: int
    tipo: TicketTipo
    cantidad: Optional[int] = None
    duracion_dias: Optional[int] = None
    fecha_inicio: Optional[date] = None
    id_cliente: int

class TarjetaMoviCard(BaseModel):
    id: int
    UUID: Optional[str] = None 
    id_suscripcion: int
    id_ticket: int
    estadotarjeta: EstadoEnum

class Destino(BaseModel):
    id: int
    transporte: TipoTransporteEnum
    inicio: str
    id_tarjetamovicard: int

class TarjetaPago(BaseModel):
    id: int
    numero: int
    titular: str
    vencimiento: str
    cvv: int