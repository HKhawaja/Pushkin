#!/usr/bin/env python
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import json, ast, os, time, hashlib, glob, datetime, sqlite3
import traceback, hmac, binascii, struct, base64, requests

def createDB():
	#this function should only ever be needed if the main DB gets deleted
	#CALL THIS FUNCTION IF YOU DO NOT HAVE A LOCAL DATABASE FOR PUSHKIN SETUP YET
	#it is primarily here for reference
	conn = sqlite3.connect('pushkin.db') #will create if doesnt exist
	c = conn.cursor()
	c.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, username text, password text, salt text, email text, pKey text, \
		tKey text, fcmToken text, proPic text, headerPic text, firstName text, lastName text)")
	c.execute("CREATE TABLE messages(id INTEGER PRIMARY KEY, recipient text, sender text, content text, tstamp text)")
	conn.commit()
	conn.close()
	print "Created Pushkin DB"

def readDB(username, value):
	#this is to get username, password, email, pKey, or tKey: which are all single values
	conn = sqlite3.connect('pushkin.db')
	c = conn.cursor()
	try:
		c.execute("SELECT {} FROM users WHERE username = '{}'".format(value, username))
		#we only need to return a single value, that is the value parameter
		value = c.fetchone()[0]
		if not value:
			return 'null'
	except TypeError as e:
		print e
		return 'null'
	except sqlite3.OperationalError as e:
		print e
		return 'null'

	return value

def readDBMessages(recipient):
	#this is to get 
	conn = sqlite3.connect('pushkin.db')
	c = conn.cursor()
	try:
		c.execute("SELECT * FROM messages WHERE recipient = ?", (recipient,))
		messages = c.fetchall()
		c.execute("DELETE FROM messages WHERE recipient = ?", (recipient,))
		conn.commit()
		#messages= c.fetchall()
	except TypeError as e:
		traceback.print_exc()
		return False
	except sqlite3.OperationalError:
		traceback.print_exc()
		return False

	return messages

def getKintacts():
	#this is to get 
	conn = sqlite3.connect('pushkin.db')
	c = conn.cursor()
	try:
		c.execute("SELECT username, firstName, lastName, proPic, pKey FROM Users")
		keys = c.fetchall()
		#print "\n\n\n", keys, "\n\n\n"
	except TypeError as e:
		traceback.print_exc()
		return False
	except sqlite3.OperationalError:
		traceback.print_exc()
		return False
	return keys

def updateDB(column, value, username):
	conn = sqlite3.connect('pushkin.db') #will create if doesnt exist
	c = conn.cursor()
	c.execute("UPDATE users set {} = '{}' WHERE username = '{}'".format(column, value, username))
	conn.commit()
	conn.close()
	return True

def store_msg(recipient, sender, message, timestamp):
	conn = sqlite3.connect('pushkin.db') #will create if doesnt exist
	pKey = readDB(recipient, 'pKey') #get the recipient's pKey
	try:
		c = conn.cursor()
		c.execute("INSERT INTO messages (recipient, sender, content, tstamp, pKey) VALUES (?, ?, ?, ?, ?)", (recipient, sender, message, timestamp, pKey))
		conn.commit()
		conn.close()
		return True
	except:
		traceback.print_exc()
		return False

def create_new_user(username, password, email, pKey, fcmToken):
	salt = base64.b64encode(os.urandom(128))
	hashed_pwd = hashlib.sha512('{}{}'.format(salt, password)).hexdigest()
	tKey = base64.b64encode(os.urandom(128))
	conn = sqlite3.connect('pushkin.db') #will create if doesnt exist

	#print username, password, email, salt, pKey, tKey, fcmToken
	try:
		c = conn.cursor()
		c.execute("INSERT INTO users (username, password, email, salt, pKey, tKey, fcmToken) \
			VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')" % (username, hashed_pwd, email, salt, pKey, tKey, fcmToken))
		conn.commit()
		conn.close()
		return True
	except:
		traceback.print_exc()
		return False

def send_msg(recipient, sender):
	"""sends notification"""
	api_key = "AIzaSyBzCojgZBmcqW9rxjkopwnQW_fE4QIU2A4"
	print("RECIPI IS" + recipient)
	recipient_token = readDB(recipient, 'fcmToken')
	print(recipient_token)
	print recipient_token
	if not recipient_token:
		return False
	#we cannot write content of message in push notification because of end-to-end encryption
	r = requests.post('https://gcm-http.googleapis.com/gcm/send', 
		headers={'Content-Type': 'application/json', 'Authorization':'key={}'.format(api_key)}, 
		json = {'to': recipient_token, "notification": {"title": "New Push", "body": "You have a new push from {}!".format(sender)}})
	print r.request.headers
	print r.request.body
	print r.content
	return True

def check_uname_pwd(username, password):
	#precondition: username exists
	stored_hashed_pwd = readDB(username, 'password')
	user_salt = readDB(username, 'salt')
	hashed_pwd = hashlib.sha512('{}{}'.format(user_salt, password)).hexdigest()
	#print stored_hashed_pwd
	#print hashed_pwd
	if stored_hashed_pwd != hashed_pwd:
		#print 'Bad Password'
		return False
	#print 'Good password'
	return True

def check_auth_format(auth):
	if len(auth.split(':')) != 2:
		return False
	return True

def check_user_exists(recipient):
	#print recipient
	db_info = readDB(recipient, 'username')
	if not db_info:
		#print db_info
		return False
	return True

def generate_secure_token(username, password):
	#here is how we generate secure tokens:
	#sha1-hmac(user_id + current_time, 1024-bit-key)
	user_key = readDB(username, 'tKey')
	if not user_key:
		return False
	current_time = int(time.time())
	hash_generator = hmac.new('{}{}'.format(username, current_time), user_key, hashlib.sha1)
	return base64.b64encode(struct.pack('>I', current_time) + hash_generator.hexdigest())

def check_secure_token(username, token):
	user_key = readDB(username, 'tKey')
	if not user_key:
		return False
	try:
		decoded_token = base64.b64decode(token)
	except:
		#bad padding or something else AKA bad token
		return False
	try:
		timestamp = struct.unpack('>I', decoded_token[:4])[0]
	except:
		return False
	supplied_hash = decoded_token[4:]
	hash_with_supplied = hmac.new('{}{}'.format(username, timestamp), user_key, hashlib.sha1).hexdigest()
	if hash_with_supplied == supplied_hash:
		return True
	return False

class handler(BaseHTTPRequestHandler):
	def set_headers(self):
		self.send_response(200)
		self.send_header('Content-type', 'text/html')
		self.end_headers()

	def do_POST(self):
		self.set_headers()
		raw_function = self.raw_requestline.split()[1]
		function = raw_function.replace('/','')
		payload = self.rfile.read(int(self.headers.getheader('content-length')))
		print '\033[92m{}\033[0m'.format(payload)
		
		if function == 'login':
			"""Expects a public key, a fcm token, a username, and a password."""
			try:
				json_obj = ast.literal_eval(payload)
				username = '{}'.format(json_obj['username'])
				password = '{}'.format(json_obj['password'])
				pKey = '{}'.format(json_obj['pKey'])
				fcmToken = '{}'.format(json_obj['fcmToken'])

				if not check_user_exists(username):
					self.wfile.write('{"Success":"0", "Reason": "Invalid username/password."}')
					#self.wfile.write("bad username")
					return

				if not check_uname_pwd(username, password):
					self.wfile.write('{"Success":"0", "Reason": "Invalid username/password."}')
					#self.wfile.write("bad pwd")
					return

				#okay, user exists, and pwd is verified.
				"""
				every time the user 'logs in' with the pushkin login screen,
				a new pKey and sKey will be generated. Furthermore, we want to
				send the current device fcmToken
				"""
				#update DB with their new pKey and their new fcmToken
				if not updateDB('pKey', pKey, username):
					self.wfile.write('{"Success":"0", "Reason": "Public key could not be updated."}')
					#self.wfile.write("No public key")
					return

				if not updateDB('fcmToken', fcmToken, username):
					self.wfile.write('{"Success":"0", "Reason": "FCM token could not be updated."}')
					return

				#updated DB. all is good, now send them a token
				token = generate_secure_token(username, password)
				if not token:
					self.wfile.write('{"Success":"0", "Reason": "Could not generate token."}')
					return

				keyJson = ""
				for keys in getKintacts():
					(uname, firstName, lastName, proPic, pubKey) = keys
					if proPic == None:
						proPic = ''
					if pubKey == None:
						pubKey = ''
					if lastName == None:
						lastName = ''
					if firstName == None:
						firstName = ''
					if username == None:
						uname = ''
					keyJson += """{"username": "%s", "firstName":"%s", "lastName":"%s", "image":"%s", "pKey": "%s"},""" % (uname, firstName, lastName, proPic.replace('\n', ''), pubKey.replace('\n', ''))
				keyJson = keyJson[:-1]
				bors = '{"Success":"1","AuthToken":"%s","email":"%s", "firstName":"%s","lastName": "%s",\
"headerPic":"%s","proPic":"%s", "kintacts": [%s]}' % (token, readDB(username, "email"), readDB(username, "firstName"),
			 	readDB(username, "lastName"), readDB(username, 'headerPic'), readDB(username, 'proPic'), keyJson)
				print bors
				self.wfile.write(bors)

				return

			except Exception as e:
				self.wfile.write('{"Success":"0", "Reason": "Malformed request."}')
				traceback.print_exc()
				return

		elif function == 'getMsg':
			try:
				json_obj = ast.literal_eval(payload)
				authorization = '{}'.format(json_obj['authorization'])
				(username, token) = authorization.split(":")
				if not check_secure_token(username, token):
					self.wfile.write('{"Success":"0", "Reason": "Invalid AuthToken"}') 
					return
				jsonable = """{"Messages": ["""
				for msg in readDBMessages(username):
					(pid, recipient, sender, content, times, pKey) = msg
					jsonable += """{"Recipient": "%s", "Sender": "%s", "Content": "%s", "Time": "%s"},""" % (recipient, sender, content, times)
				jsonable += "]}"
				self.wfile.write(jsonable)
				return
			except Exception as e:
				self.wfile.write('{"Success":"0", "Reason": "Malformed request."}')
				traceback.print_exc()
				return

		elif function == 'createNewUser':
			try:
				json_obj = ast.literal_eval(payload)
				username = '{}'.format(json_obj['username'])
				password = '{}'.format(json_obj['password'])
				email = '{}'.format(json_obj['email'])
				pKey = '{}'.format(json_obj['pKey'])
				fcmToken = '{}'.format(json_obj['fcmToken'])

				if check_user_exists(username):
					self.wfile.write('{"Success":"0", "Reason": "Username already exists."}')
					return

				user = create_new_user(username, password, email, pKey, fcmToken)
				
				if not user:
					self.wfile.write('{"Success":"0", "Reason": "Error creating user."}')
					return False
				
				#user has been created, now generate a token for them
				token = generate_secure_token(username, password)
				if not token:
					self.wfile.write('{"Success":"0", "Reason": "Error creating token."}')
					return
				#send this token over
				#print 'Created user!'
				self.wfile.write('{"Success":"1", "AuthToken": "%s"}' % token) 
				return
			except:
				self.wfile.write('[x] Malfunction in server.\n')
				traceback.print_exc()
				raise e 

		elif function == 'getPubKey':
			"""this function will return the public key of a user"""
			try:
				json_obj = ast.literal_eval(payload)
				username = '{}'.format(json_obj['username'])

				if not check_user_exists(username):
					self.wfile.write('{"Success":"0", "Reason": "User does not exist."}')
					return

				key = readDB(username, 'pKey')
				if not key:
					self.wfile.write('{"Success":"0", "Reason": "Error reading key."}')
					return
				
				self.wfile.write('{"Success":"1", "pKey": "%s"}' % key)
				return
				
			except:
				self.wfile.write('{"Success":"0", "Reason": "Malfunction in server."}')
				traceback.print_exc()
				return

		elif function == 'sendMsg':
			"""
			This function will store the message securely in the folder of the recipient
			and then sending a push notification to the recipient
			"""
			try:
				json_obj = ast.literal_eval(payload)
				recipient = '{}'.format(json_obj['recipient'])
				message = '{}'.format(json_obj['message']).decode('utf-8')
				authorization = '{}'.format(json_obj['authorization'])
				
				"""Authorization must be a token to send a message"""
				
				timestamp = int(time.time()) #truncate at ms
				timestamp = datetime.datetime.fromtimestamp(timestamp).strftime("%I:%M %p")
				
				username = authorization.split(':')[0]
				token = authorization.split(':')[1]
			
				#check if the authorization is valid
				if not check_secure_token(username, token):
					self.wfile.write('{"Success":"0", "Reason": "Invalid AuthToken"}') 
					return
				
				#does the recipient exist
				if not check_user_exists(recipient):
					self.wfile.write('{"Success":"0", "Reason": "Recipient does not exist"}') 
					return

				sender = username
				print "SOMETHING"

				if store_msg(recipient, sender, message, timestamp):
					#stored message for recipient
					#now send the recipient a notifiction
					if send_msg(recipient, sender):
						self.wfile.write('{"Success":"1", "Message": "Message sent to %s"}' % recipient) 
					else:
						self.wfile.write('{"Success":"1", "Message": "Message stored, but not pushed"}') 
				else:
					self.wfile.write('{"Success":"0", "Reason": "Could not store message"}') 
				return

			except KeyError:
				self.wfile.write('{"Success":"0", "Reason": "Malformed request."}')
				return

			except Exception as e:
				self.wfile.write('{"Success":"0", "Reason": "Malfunction in server."}')
				traceback.print_exc()
				return

		self.wfile.write('[x] Invalid function.')

	def do_GET(self):
		self.set_headers()
		requested_name = self.raw_requestline.split()[1]
		if requested_name.split('?')[0] == '/getSomething':
			identifier = requested_name.split('?')[1].split('=')
			if identifier[0] != 'id':
				self.wfile.write('[x] Malformed request.\n')
				return
			else:
				#do some stuff
				self.wfile.write(json.dumps(dict_content))
		else:
			self.wfile.write('[x] Malformed request.\n')
			return
		return
		
def run():
	http_serv = HTTPServer(('', 8080), handler)
	#if not os.path.isdir('userdata'):
	#	os.mkdir('userdata')
	#os.chdir('userdata')
	print('Starting server')
	http_serv.serve_forever()

if __name__ == "__main__":
	run()