import logging
import json
from google.cloud import storage
from google.cloud import datastore


def update_data(event, context):
    """Triggered by a change to a Cloud Storage bucket.
    Args:
         event (dict): Event payload.
         context (google.cloud.functions.Context): Metadata for the event.
    """
    file = event

    if file['name'] == 'stopcounts.json':
        update_truck_counts()
    print(f"Processing file: {file['name']}.")


def retrieve_file(file_name):
    storage_client = storage.Client()
    bucket_name = 'chiftf_aggregate_data'
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(file_name)
    counts_value = blob.download_as_string()
    return json.loads(counts_value)


def update_truck_counts():
    contents = retrieve_file("stopcounts.json")
    datastore_client = datastore.Client()
    kind = 'Store'
    for count in contents:
        key = datastore_client.key(kind, count['truckId'])
        truck = datastore_client.get(key)
        if truck is not None:
            try:
                logging.info('Retrieved {}'.format(count['truckId']))
                truck['all_stops'] = int(count['f0_'])
                datastore_client.put(truck)
            except TypeError:
                logging.error("An error occurred")
        else:
            logging.error(f"Could not retrieve {count['truckId']}")


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    update_data({'name': 'stopcounts.json'}, None)
