from typing import Optional
from fastapi import APIRouter, Body, HTTPException
from baseDeDatos import connect_to_db, get_cursor
import uuid

from modelo import EstadoTarjeta, LoginData, PlanEnum
from datetime import date

router = APIRouter()

# --- LOGIN ---


@router.post("/api/login/")
async def login(data: LoginData):
    correo = data.correo
    password = data.password


    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
   
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM cliente WHERE correo = %s AND password = %s", (correo, password))
        cliente = cursor.fetchone()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
   
    if not cliente:
        raise HTTPException(status_code=401, detail="Credenciales incorrectas")
   
    return {
        "message": "Login exitoso",
        "cliente": {
            "id": cliente[0],
            "nombre": cliente[1],
            "apellido": cliente[2],
            "dni": cliente[3],
            "correo": cliente[4],
            "telefono": cliente[5],
            "direccion": cliente[6],
            "numero_bloque": cliente[7],
            "numero_piso": cliente[8],
            "codigopostal": cliente[9],
            "ciudad": cliente[10],
            "password": "oculto"  # Por seguridad
        }
    }



# --- CLIENTES ---

@router.get("/get/clientes/")
async def get_all_clientes():
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM cliente")
        rows = cursor.fetchall()
        if not rows:
            raise HTTPException(status_code=404, detail="No se encontraron clientes.")

        column_names = [desc[0] for desc in cursor.description]
        clientes = [dict(zip(column_names, row)) for row in rows]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return clientes


@router.get("/get/clientes/{id}")
async def get_cliente(id: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM cliente WHERE id = %s", (id,))
        cliente = cursor.fetchone()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    
    return {
        "id": cliente[0],
        "nombre": cliente[1],
        "apellido": cliente[2],
        "dni": cliente[3],
        "correo": cliente[4],
        "telefono": cliente[5],
        "direccion": cliente[6],
        "numero_bloque": cliente[7],
        "numero_piso": cliente[8],
        "codigopostal": cliente[9],
        "ciudad": cliente[10],
        "password": cliente[11]
    }

@router.post("/post/cliente/")
async def create_cliente(nombre: str, apellido: str, dni: str, correo: str, telefono: str,
                         direccion: str, numero_bloque: str, codigopostal: str,
                         ciudad: str, password: str,  numero_piso: Optional[str] = None):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "INSERT INTO cliente (nombre, apellido, dni, correo, telefono, direccion, numero_bloque, numero_piso, codigopostal, ciudad, password) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            (nombre, apellido, dni, correo, telefono, direccion, numero_bloque, numero_piso, codigopostal, ciudad, password)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al insertar cliente: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return {"message": "Cliente creado exitosamente"}

@router.put("/put/cliente/{id}")
async def update_cliente(id: int, nombre: str, apellido: str, dni: str, correo: str, telefono: str,
                         direccion: str, numero_bloque: str, numero_piso: str, codigopostal: str,
                         ciudad: str, password: str):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "UPDATE cliente SET nombre = %s, apellido = %s, dni = %s, correo = %s, telefono = %s, direccion = %s, "
            "numero_bloque = %s, numero_piso = %s, codigopostal = %s, ciudad = %s, password = %s WHERE id = %s",
            (nombre, apellido, dni, correo, telefono, direccion, numero_bloque, numero_piso, codigopostal, ciudad, password, id)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al actualizar cliente: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return {"message": "Cliente actualizado exitosamente"}

@router.put("/put/cliente/password/{id}")
async def update_password(id: int, actual: str, nueva: str):
    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)
        # Verifica si la contraseña actual coincide
        cursor.execute("SELECT password FROM cliente WHERE id = %s", (id,))
        result = cursor.fetchone()
        if not result or result[0] != actual:
            raise HTTPException(status_code=400, detail="Contraseña actual incorrecta")

        # Actualiza la contraseña
        cursor.execute("UPDATE cliente SET password = %s WHERE id = %s", (nueva, id))
        connection.commit()
        return {"message": "Contraseña actualizada exitosamente"}
    finally:
        cursor.close()
        connection.close()


@router.delete("/delete/clientes/{id}")
async def delete_cliente(id: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("DELETE FROM cliente WHERE id = %s", (id,))
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al eliminar cliente: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return {"message": "Cliente eliminado exitosamente"}

# --- TARJETAS ---

@router.get("/get/tarjetas/")
async def get_all_tarjetas():
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM tarjetamovicard")
        rows = cursor.fetchall()

        column_names = [desc[0] for desc in cursor.description]
        tarjetas = [dict(zip(column_names, row)) for row in rows]

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return tarjetas

@router.get("/get/tarjeta/{id_cliente}")
async def getTarjetaByClienteId(id_cliente: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM tarjetamovicard WHERE id_cliente = %s", (id_cliente,))
        tarjeta = cursor.fetchone()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    if not tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    
    return {
        "id": tarjeta[0],
        "UUID": tarjeta[1],
        "id_cliente": tarjeta[2],
        "id_suscripcion": tarjeta[3],
        "id_ticket": tarjeta[4],
        "estadotarjeta": tarjeta[5],
        "estadoactivaciontarjeta": tarjeta[6]
    }

@router.post("/post/tarjeta/")
async def crear_tarjeta(id_cliente: int, id_suscripcion: int, id_ticket: Optional[int] = None):
    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)

        # Verificar si ya existe una tarjeta para este cliente
        cursor.execute("SELECT * FROM tarjetamovicard WHERE id_cliente = %s", (id_cliente,))
        existente = cursor.fetchone()
        if existente:
            raise HTTPException(status_code=409, detail="Ya existe una tarjeta para este cliente.")

        uuid_generado = str(uuid.uuid4())
        cursor.execute("""
            INSERT INTO tarjetamovicard (UUID, id_cliente, id_suscripcion, id_ticket, estadotarjeta, estadoactivaciontarjeta)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (uuid_generado, id_cliente, id_suscripcion, id_ticket, "BLOQUEADA", "DESACTIVADA"))
        connection.commit()

        return {"message": "Tarjeta creada exitosamente", "UUID": uuid_generado}
    finally:
        cursor.close()
        connection.close()



@router.put("/put/tarjeta/estado/{id_cliente}")
async def actualizarEstadoTarjeta(id_cliente: int, nuevo_estado: str = Body(..., media_type="text/plain")):
    if nuevo_estado not in ["ACTIVADA", "BLOQUEADA"]:
        raise HTTPException(status_code=400, detail="Estado no válido. Debe ser 'ACTIVADA' o 'BLOQUEADA'.")

    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT id FROM tarjetamovicard WHERE id_cliente = %s", (id_cliente,))
        tarjeta = cursor.fetchone()
        if not tarjeta:
            raise HTTPException(status_code=404, detail="Tarjeta no encontrada")

        id_tarjeta = tarjeta[0]  # Esto es el ID real de la tarjeta, no el cliente
        
        cursor.execute(
            "UPDATE tarjetamovicard SET estadotarjeta = %s WHERE id = %s",
            (nuevo_estado, id_tarjeta)
        )
        connection.commit()
        return {"message": f"Estado de la tarjeta actualizado a {nuevo_estado}"}
    finally:
        cursor.close()
        connection.close()

@router.put("/put/tarjeta/activacion/{id_cliente}")
async def actualizarEstadoTarjeta(id_cliente: int, nuevo_estado_activacion: str = Body(..., media_type="text/plain")):
    if nuevo_estado_activacion not in ["ACTIVADA", "DESACTIVADA"]:
        raise HTTPException(status_code=400, detail="Estado no válido. Debe ser 'ACTIVADA' o 'DESACTIVADA'.")

    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT id FROM tarjetamovicard WHERE id_cliente = %s", (id_cliente,))
        tarjeta = cursor.fetchone()
        if not tarjeta:
            raise HTTPException(status_code=404, detail="Tarjeta no encontrada")

        id_tarjeta = tarjeta[0]  # Esto es el ID real de la tarjeta, no el cliente
        
        cursor.execute(
            "UPDATE tarjetamovicard SET estadoactivaciontarjeta = %s WHERE id = %s",
            (nuevo_estado_activacion, id_tarjeta)
        )
        connection.commit()
        return {"message": f"Estado de activación de la tarjeta actualizado a {nuevo_estado_activacion}"}
    finally:
        cursor.close()
        connection.close()

@router.put("/put/tarjeta/ticket/{id_cliente}")
async def actualizar_id_ticket(id_cliente: int, nuevo_id_ticket: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")

    try:
        cursor = get_cursor(connection)

        cursor.execute("SELECT id FROM tarjetamovicard WHERE id_cliente = %s", (id_cliente,))
        tarjeta = cursor.fetchone()
        if not tarjeta:
            raise HTTPException(status_code=404, detail="Tarjeta no encontrada")

        cursor.execute(
            "UPDATE tarjetamovicard SET id_ticket = %s WHERE id_cliente = %s",
            (nuevo_id_ticket, id_cliente)
        )
        connection.commit()

        return {"message": f"id_ticket actualizado correctamente a {nuevo_id_ticket}"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")
    finally:
        cursor.close()
        connection.close()



@router.delete("/delete/tarjetas/{id}")
async def delete_tarjeta(id: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("DELETE FROM tarjetamovicard WHERE id = %s", (id,))
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al eliminar tarjeta: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return {"message": "Tarjeta eliminada exitosamente"}






# --- SUSCRIPCIÓN ---

@router.get("/get/suscripcion/{id_cliente}")
async def get_suscripcion_by_cliente(id_cliente: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")

    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT id, suscripcion, id_cliente FROM suscripcion WHERE id_cliente = %s", (id_cliente,))
        result = cursor.fetchone()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    if not result:
        raise HTTPException(status_code=404, detail="Suscripción no encontrada")

    return {
        "id": result[0],
        "suscripcion": result[1],
        "id_cliente": result[2]
    }

@router.post("/post/suscripcion/")
async def crear_suscripcion(id_cliente: int, suscripcion: PlanEnum = PlanEnum.GRATUITA):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "INSERT INTO suscripcion (suscripcion, id_cliente) VALUES (%s, %s)",
            (suscripcion.value, id_cliente)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al insertar la suscripción: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    return {"message": "Suscripción creada correctamente", "suscripcion": suscripcion}

@router.put("/put/suscripcion/premium/{id_cliente}")
async def actualizar_suscripcion_a_premium(id_cliente: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "UPDATE suscripcion SET suscripcion = %s WHERE id_cliente = %s",
            (PlanEnum.PREMIUM.value, id_cliente)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al actualizar la suscripción: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    return {"message": "Suscripción actualizada a PREMIUM"}


@router.put("/put/suscripcion/gratuita/{id_cliente}")
async def actualizar_suscripcion_a_premium(id_cliente: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "UPDATE suscripcion SET suscripcion = %s WHERE id_cliente = %s",
            (PlanEnum.GRATUITA.value, id_cliente)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al actualizar la suscripción: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    return {"message": "Suscripción actualizada a GRATUITA"}




# --- TICKETS ---

@router.get("/get/tickets/{id_cliente}")
async def getTicketByClienteId(id_cliente: int):
    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM ticket WHERE id_cliente = %s", (id_cliente,))
        ticket = cursor.fetchone()

        if not ticket:
            raise HTTPException(status_code=404, detail="No se encontró ticket para este cliente")

        return {
            "id": ticket[0],
            "tipo": ticket[1],
            "cantidad": ticket[2],
            "duracion_dias": ticket[3],
            "fecha_inicio": ticket[4],
            "id_cliente": ticket[5]
        }
    finally:
        cursor.close()
        connection.close()



@router.post("/post/tickets/")
async def create_ticket(tipo: str, id_cliente: int):
    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)

        # Verificar si ya existe un ticket para este cliente
        cursor.execute("SELECT id FROM ticket WHERE id_cliente = %s", (id_cliente,))
        existing = cursor.fetchone()
        if existing:
            raise HTTPException(status_code=409, detail="El cliente ya tiene un ticket asignado.")

        cantidad = 0
        duracion_dias = None
        fecha_inicio = date.today()

        if tipo == "TENMOVI":
            cantidad = 10
        elif tipo == "MOVIMES":
            cantidad = -1
            duracion_dias = 30
        elif tipo == "TRIMOVI":
            cantidad = -1
            duracion_dias = 90
        else:
            raise HTTPException(status_code=400, detail="Tipo de ticket no válido.")

        cursor.execute(
            "INSERT INTO ticket (tipo, cantidad, duracion_dias, fecha_inicio, id_cliente) "
            "VALUES (%s, %s, %s, %s, %s)",
            (tipo, cantidad, duracion_dias, fecha_inicio, id_cliente)
        )
        connection.commit()
        return {"message": "Ticket creado exitosamente"}

    finally:
        cursor.close()
        connection.close()

@router.put("/put/tickets/{id_cliente}")
async def update_ticket(id_cliente: int, tipo: str = Body(..., media_type="text/plain")):
    connection = connect_to_db()
    cantidad = None
    duracion_dias = None
    fecha_inicio = date.today()

    if tipo == "TENMOVI":
        cantidad = 10
    elif tipo == "MOVIMES":
        duracion_dias = 30
    elif tipo == "TRIMOVI":
        duracion_dias = 90
    else:
        raise HTTPException(status_code=400, detail="Tipo de ticket no válido")

    try:
        cursor = get_cursor(connection)

        # Obtener el ID real del ticket basado en el cliente
        cursor.execute("SELECT id FROM ticket WHERE id_cliente = %s", (id_cliente,))
        ticket = cursor.fetchone()
        if not ticket:
            raise HTTPException(status_code=404, detail="Ticket no encontrado")

        ticket_id = ticket[0]

        # Actualizar ticket con el ID correcto
        cursor.execute(
            """
            UPDATE ticket
            SET tipo = %s,
                cantidad = %s,
                duracion_dias = %s,
                fecha_inicio = %s
            WHERE id = %s
            """,
            (tipo, cantidad, duracion_dias, fecha_inicio, ticket_id)
        )
        connection.commit()
        return {"message": "Ticket actualizado correctamente"}
    finally:
        cursor.close()
        connection.close()


@router.put("/put/tickets/cantidad/{id_cliente}")
async def actualizar_cantidad_ticket(id_cliente: int, nueva_cantidad: int = Body(...)):
    connection = connect_to_db()
    try:
        cursor = get_cursor(connection)

        # Verificar que el ticket existe y que es TENMOVI
        cursor.execute("SELECT id, tipo FROM ticket WHERE id_cliente = %s", (id_cliente,))
        ticket = cursor.fetchone()

        if not ticket:
            raise HTTPException(status_code=404, detail="No se encontró ticket para este cliente")

        ticket_id, tipo = ticket

        if tipo != "TENMOVI":
            raise HTTPException(status_code=400, detail="Solo se puede actualizar cantidad si el tipo es TENMOVI")

        # Actualizar la cantidad
        cursor.execute(
            "UPDATE ticket SET cantidad = %s WHERE id = %s",
            (nueva_cantidad, ticket_id)
        )
        connection.commit()

        return {"message": "Cantidad de ticket actualizada correctamente", "nueva_cantidad": nueva_cantidad}

    finally:
        cursor.close()
        connection.close()