import mysql.connector

try:
    connection = mysql.connector.connect(
        host="10.0.82.1",
        user="christopher",
        password="christopher",
        database="movicard"
    )
    print("Conexión exitosa!")
    connection.close()
except mysql.connector.Error as err:
    print(f"Error al conectar con la base de datos: {err}")