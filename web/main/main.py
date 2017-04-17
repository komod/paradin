import datetime

from google.cloud import datastore
from flask import Flask, request

app = Flask(__name__)
client = datastore.Client('paradin-me')

LATITUDE_KEY = 'latitude'
LONGITUDE_KEY = 'longitude'

@app.route('/')
def hello():
    return 'Hello World!'

@app.route('/route/api/v1.0/anchor', methods=['POST'])
def add_anchor():
    if request.json:
        anchor = datastore.Entity(client.key('anchor'),
            exclude_from_indexes=['latitude', 'longitude'])
        anchor.update({
            LATITUDE_KEY: request.json.get(LATITUDE_KEY, 0),
            LONGITUDE_KEY: request.json.get(LONGITUDE_KEY, 0),
            'created': datetime.datetime.utcnow(),
            })
        client.put(anchor)

    return 'OK'
