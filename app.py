from flask import Flask, redirect, url_for, session, request

import os 
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

from flask_oauthlib.client import OAuth

SECRET_KEY = 'developm'
DEBUG = True

OAUTH_CLIENT_ID = 'this_is_the_key'
OAUTH_CLIENT_SECRET = 'this_is_the_secret'

OAUTH_URL = 'http://localhost:8081'
OAUTH_AUTHORIZE_URL = '/cas/oauth2.0/authorize'
OAUTH_ACCESS_URL = '/cas/oauth2.0/accessToken'
OAUTH_MORE = '/cas/oauth2.0/profile'


app = Flask(__name__)
app.debug = DEBUG
app.secret_key = SECRET_KEY
oauth = OAuth()

oauth_generic = oauth.remote_app('generic',
    base_url=OAUTH_URL,
    request_token_params=None,
    request_token_url=None,
    authorize_url=OAUTH_AUTHORIZE_URL,

    access_token_url=OAUTH_ACCESS_URL,

    consumer_key=OAUTH_CLIENT_ID,
    consumer_secret=OAUTH_CLIENT_SECRET,
)


@app.route('/')
def index():
    return redirect(url_for('login'))


@app.route('/login')
def login():
    return oauth_generic.authorize(callback=url_for('oauth_authorized',
        next=request.args.get('next') or request.referrer or None,
        _external=True))


@app.route('/login/authorized')
@oauth_generic.authorized_handler
def oauth_authorized(resp):
    if resp is None:
        return 'Access denied: reason=%s error=%s' % (
            request.args['error_reason'],
            request.args['error_description']
        )
    print resp
    session['oauth_token'] = (resp['access_token'], '')

    if OAUTH_MORE:
        me = oauth_generic.get(OAUTH_MORE + "?access_token=" + session['oauth_token'][0])
        print me.data
        return str(me.data)
    return "Success"

@oauth_generic.tokengetter
def get_oauth_token():
    return session.get('oauth_token')


if __name__ == '__main__':
    app.run()

