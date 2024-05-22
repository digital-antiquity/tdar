# Running tDAR in Docker

## Building tDAR image

To run tDAR in Docker, first build the tDAR image by running `docker build -t tdar-web .` from the root directory (where the Dockerfile is located). Depending on your operating system, you might have to change the platform argument in the `FROM` line, the phantomjs version, and remove the line `ENV QT_QPA_PLATFORM offscreen`.

## Creating database folders

In the `data` directory, create three folders: `db-data`, `db-gis`, `db-meta`. The final folder structure should look like this:

```
- data
   - data
      - tdar
         - filestore
         - hosted-filestore
         - personal-filestore
   - db-data
   - db-gis
   - db-meta
```

## Running the containers

Once you have a tdar-web image, you can start docker-compose by typing `docker-compose up` (or `docker compose up` for newer Docker versions) in the directory with the docker-compose.yaml file.

## Setting db configs

Once all containers are running, follow the instructions to set up tDAR in `install.md`, then open `web/src/main/resources/hibernate.properties` and adjust the settings as follows:

- Instead of `dtar.database.host`, add three properties:
    ```
    tdar.database.host.metadata=db-meta:5432
    tdar.database.host.data=db-data:5432
    tdar.database.host.gis=db-gis:5432
    ```
- Set username and password to `tdar`:
    ```
    javax.persistence.jdbc.user=tdar
    javax.persistence.jdbc.password=tdar
    ```
- Change the following database properties:
   - `javax.persistence.jdbc.url=jdbc:postgresql://${tdar.database.host.metadata}/tdarmetadata`
   - `tdardata.persistence.jdbc.url=jdbc:postgresql://${tdar.database.host.data}/tdardata`
   - `tdargisdata.persistence.jdbc.url=jdbc:postgresql_postGIS://${tdar.database.host.gis}/tdargis`

Now you can continue with "setting up the database(s) with test data" (in `install.md`).

