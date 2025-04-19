from fastapi import APIRouter, HTTPException
from baseDeDatos import connect_to_db, get_cursor
import uuid

router = APIRouter()

# --- CLIENTES ---

@router.get("/api/get/clientes/")
async def get_all_clientes():
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM cliente")
        clientes = cursor.fetchall()
        if not clientes:
            raise HTTPException(status_code=404, detail="No se encontraron clientes.")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return clientes

@router.get("/api/get/clientes/{id}")
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

@router.post("/api/crea/cliente/")
async def create_cliente(nombre: str, apellido: str, dni: str, correo: str, telefono: str,
                         direccion: str, numero_bloque: str, numero_piso: str, codigopostal: str,
                         ciudad: str, password: str):
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

@router.put("/api/modifica/cliente/{id}")
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

@router.delete("/api/elimina/clientes/{id}")
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

@router.get("/api/tarjetas/")
async def get_all_tarjetas():
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM tarjetamovicard")
        tarjetas = cursor.fetchall()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al consultar la base de datos: {str(e)}")
    finally:
        cursor.close()
        connection.close()
    
    return tarjetas

@router.get("/api/tarjetas/{id}")
async def get_tarjeta(id: int):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")
    
    try:
        cursor = get_cursor(connection)
        cursor.execute("SELECT * FROM tarjetamovicard WHERE id = %s", (id,))
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
        "id_saldo": tarjeta[4],
        "estadotarjeta": tarjeta[5]
    }

@router.post("/api/tarjetas/")
async def create_tarjeta(id_cliente: int, id_suscripcion: int, id_saldo: int, estadotarjeta: str, UUID: str = None):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")

    if UUID is None:
        UUID = str(uuid.uuid4()) 

    try:
        cursor = get_cursor(connection)
        cursor.execute(
            "INSERT INTO tarjetamovicard (UUID, id_cliente, id_suscripcion, id_saldo, estadotarjeta) VALUES (%s, %s, %s, %s, %s)",
            (UUID, id_cliente, id_suscripcion, id_saldo, estadotarjeta)
        )
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al insertar tarjeta: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    return {"message": "Tarjeta creada exitosamente", "UUID": UUID}

@router.put("/api/tarjetas/{id}")
async def update_tarjeta(id: int, id_cliente: int, id_suscripcion: int, id_saldo: int, estadotarjeta: str):
    connection = connect_to_db()
    if not connection:
        raise HTTPException(status_code=500, detail="Error al conectar con la base de datos.")

    try:
        cursor = get_cursor(connection)

        cursor.execute("SELECT UUID FROM tarjetamovicard WHERE id = %s", (id,))
        tarjeta = cursor.fetchone()

        if not tarjeta:
            raise HTTPException(status_code=404, detail="Tarjeta no encontrada.")

        UUID = tarjeta[0]  

        cursor.execute(
            "UPDATE tarjetamovicard SET id_cliente = %s, id_suscripcion = %s, id_saldo = %s, estadotarjeta = %s WHERE id = %s",
            (id_cliente, id_suscripcion, id_saldo, estadotarjeta, id)
        )
        connection.commit()

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al actualizar tarjeta: {str(e)}")
    finally:
        cursor.close()
        connection.close()

    return {"message": "Tarjeta actualizada exitosamente", "UUID": UUID}


@router.delete("/api/tarjetas/{id}")
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
