import mariadb

def connect_to_db():
    try:
        connection = mariadb.connect(
            host="localhost",
            user="root",
            password="12345",
            database="movicard"
        )
        return connection
    except mariadb.Error as err:
        raise Exception(f"Error de conexión: {err}")

def get_cursor(connection):
    return connection.cursor()