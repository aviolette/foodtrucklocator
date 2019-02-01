import logging
import json
from google.cloud import storage


def update_data(event, context):
    """Triggered by a change to a Cloud Storage bucket.
    Args:
         event (dict): Event payload.
         context (google.cloud.functions.Context): Metadata for the event.
    """
    file = event
    logging.info(file)

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
    for count in contents:
        print(f"{count['truckId']}")
    pass


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    update_data({'name': 'stopcounts.json'}, None)
