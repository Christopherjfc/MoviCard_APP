import mariadb

try:
    connection = mariadb.connect(
        host="localhost",
        user="root",
        password="12345",
        database="movicard",
        port=3306
    )
    print("Conexión exitosa!")
    connection.close()
except mariadb.Error as err:
    print(f"Error al conectar con la base de datos: {err}")