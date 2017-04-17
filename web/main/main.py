import datetime

from google.cloud import datastore
from flask import Flask, render_template, request, jsonify

app = Flask(__name__)
client = datastore.Client('paradin-me')

ANCHOR_KIND = 'anchor'
LATITUDE_KEY = 'latitude'
LONGITUDE_KEY = 'longitude'

@app.route('/')
def hello():
    return 'Hello World!'

@app.route('/moonportma')
def render_moonportma():
    return render_template('moonportma.html')

@app.route('/route/api/v1.0/anchor', methods=['POST'])
def add_anchor():
    if request.json:
        anchor = datastore.Entity(client.key(ANCHOR_KIND),
            exclude_from_indexes=['latitude', 'longitude'])
        anchor.update({
            LATITUDE_KEY: request.json.get(LATITUDE_KEY, 0),
            LONGITUDE_KEY: request.json.get(LONGITUDE_KEY, 0),
            'created': datetime.datetime.utcnow(),
            })
        client.put(anchor)

    return 'OK'

@app.route('/route/api/v1.0/anchor', methods=['GET'])
def get_anchor():
    limit = int(request.args.get('limit', '0'))
    query = client.query(kind=ANCHOR_KIND, order=['-created'])
    if limit > 0:
        anchors = list(query.fetch(limit))
    else:
        anchors = list(query.fetch())
    path = []
    for ach in anchors:
        path.append({
            LATITUDE_KEY: ach.get(LATITUDE_KEY, 0),
            LONGITUDE_KEY: ach.get(LONGITUDE_KEY, 0)
            })
    return jsonify(path)
