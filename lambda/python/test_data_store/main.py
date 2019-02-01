from google.cloud import datastore


def hello_world(request):
    """Responds to any HTTP request.
    Args:
        request (flask.Request): HTTP request object.
    Returns:
        The response text or any set of values that can be turned into a
        Response object using
        `make_response <http://flask.pocoo.org/docs/1.0/api/#flask.Flask.make_response>`.
    """



    # Instantiates a client
    datastore_client = datastore.Client()

    # The kind for the new entity
    kind = 'Store'
    # The name/ID for the new entity
    name = '5411empanadas'
    # The Cloud Datastore key for the new entity
    key = datastore_client.key(kind, name)

    truck = datastore_client.get(key)

    print('Retrieved {}: {}'.format(truck.key.name, truck['last_seen_lng']))
    return "hello world"


def read_file():
    pass
