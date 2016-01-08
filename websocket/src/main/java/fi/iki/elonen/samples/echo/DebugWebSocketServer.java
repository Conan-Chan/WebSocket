package fi.iki.elonen.samples.echo;

/*
 * #%L
 * NanoHttpd-Websocket
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * @author Paul S. Hawke (paul.hawke@gmail.com) On: 4/23/14 at 10:31 PM
 */
public class DebugWebSocketServer extends NanoWSD {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(DebugWebSocketServer.class.getName());

    private final boolean debug;

    public DebugWebSocketServer(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DebugWebSocket(this, handshake);
    }

    private static class DebugWebSocket extends WebSocket {

        private final DebugWebSocketServer server;

        public DebugWebSocket(DebugWebSocketServer server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                message.setUnmasked();
                // analyse
                String textmsg = message.getTextPayload();
                // parsing json data
                JsonObject amsg = null;
                JsonReader jsonReader = Json.createReader(new StringReader(textmsg));
                amsg = jsonReader.readObject();
                // dealing with messages
                String method = amsg.getString("method");
                JsonObjectBuilder rel = Json.createObjectBuilder();
                String smsg;

                if (!method.equals("breathe")) {
                    System.out.println("[REQUEST] " + textmsg);
                }

                if (method.contains("User")) {
                     rel = dealWithUser(amsg);
                } else if (method.contains("Model")) {
                    rel = dealWithModel(amsg);
                } else if (method.contains("Property")) {
                    rel = dealWithProperty(amsg);
                    } else if (method.contains("Task")) {
                        rel = dealWithTask(amsg);
                        } else {
                    // FIXME
                    rel = dealWithOthers(amsg);
                }

                // FIXME before the message was sent, where's something to do
                if (amsg.getString("msgid", "empty") != "empty")
               {rel.add("msgid", amsg.getString("msgid"));}
                // FIXME how to generate the response String from a
                // JsonObjectBuilder
                // manually generation of json string may lead to lots of
                // literal errors
                // using a jsonwriter could be much more efficient
                StringWriter sw = new StringWriter();
                JsonWriter jwriter = Json.createWriter(sw);
                jwriter.write(rel.build());
                smsg = sw.toString();
                if (smsg != null) {
                    message.setTextPayload(smsg);
                    sendFrame(message);
                    if (!method.equals("breathe")) {
                        System.out.println("[RESPONSE]" + smsg);
                    }
                }
                // ignore repeating "breathe" message
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // FIXME I strongly suggest that these functions should be realized AS
        // JsonObject -> JsonObjectBuilder
        protected JsonObjectBuilder dealWithOthers(JsonObject msg) {
            JsonObjectBuilder rel = Json.createObjectBuilder();
            String method = msg.getString("method");
            if (method.equals("login")) {
                rel.add("method", "provideSession");
                rel.add("status", "true");
                rel.add("session", "101");
            }
            	else if (method.equals("requestID")){
            	rel.add("method", "provideID")
            	.add("ID", "123");
            }         
            // to keep active of the websocket
            else if (method.equals("breathe")) {
                // FIXME
                rel.add("method", "breathe");
            }else {
                rel.add("method", "unhandled");
            }
            return rel;
        }


        protected JsonObjectBuilder dealWithUser(JsonObject msg) {
            JsonObjectBuilder rel = Json.createObjectBuilder();
            String method = msg.getString("method");
            if (method.equals("createUser")) {
            	rel.add("method", "answerCreateUser")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("saveUser")) {
            	rel.add("method", "answerSaveUser")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("listUser")) {
            	rel.add("method", "provideListUser")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("requireUser")) {
            	//just an example
            	JsonObject user = Json.createObjectBuilder().add("id", 1)
            			.add("username","Joe").build();
            	rel.add("method", "provideUser")
            	.add("status", true)
            	.add("message", "strings")
            	.add("desc", user);
            } else
            	rel.add("method", "answerRemoveUser")
            	.add("status", true)
            	.add("message", "strings");
            return rel;
        }

        protected JsonObjectBuilder dealWithModel(JsonObject msg) {
            JsonObjectBuilder rel = Json.createObjectBuilder();
            String method = msg.getString("method");
            if (method.equals("createModel")) {
            	rel.add("method", "answerCreateModel")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("saveModel")) {
            	rel.add("method", "answerSaveModel")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("listModel")) {
            	rel.add("method", "provideListModel")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("requireModel")) {
            	//just an example
            	JsonObject Model = Json.createObjectBuilder().add("id", 1)
            			.add("Modelname","LTL").build();
            	rel.add("method", "provideModel")
            	.add("status", true)
            	.add("message", "strings")
            	.add("desc", Model);
            } else
            	rel.add("method", "answerRemoveModel")
            	.add("status", true)
            	.add("message", "strings");
            return rel;
        }

        protected JsonObjectBuilder dealWithProperty(JsonObject msg) {
            JsonObjectBuilder rel = Json.createObjectBuilder();
            String method = msg.getString("method");
            if (method.equals("createProperty")) {
            	rel.add("method", "answerCreateProperty")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("saveProperty")) {
            	rel.add("method", "answerSaveProperty")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("listProperty")) {
            	rel.add("method", "provideListProperty")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("requireProperty")) {
            	//just an example
            	JsonObject Property = Json.createObjectBuilder().add("id", 1)
            			.add("Propertyname","Safety").build();
            	rel.add("method", "provideProperty")
            	.add("status", true)
            	.add("message", "strings")
            	.add("desc", Property);
            } else
            	rel.add("method", "answerRemoveProperty")
            	.add("status", true)
            	.add("message", "strings");
            return rel;
        }

        protected JsonObjectBuilder dealWithTask(JsonObject msg) {
            JsonObjectBuilder rel = Json.createObjectBuilder();
            String method = msg.getString("method");
            if (method.equals("createTask")) {
            	rel.add("method", "answerCreateTask")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("saveTask")) {
            	rel.add("method", "answerSaveTask")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("listTask")) {
            	rel.add("method", "provideListTask")
            	.add("status", true)
            	.add("message", "strings");
            } else if (method.equals("requireTask")) {
            	//just an example
            	JsonObject Task = Json.createObjectBuilder().add("id", 1)
            			.add("Taskname","Task").build();
            	rel.add("method", "provideTask")
            	.add("status", true)
            	.add("message", "strings")
            	.add("desc", Task);
            } else
            	rel.add("method", "answerRemoveTask")
            	.add("status", true)
            	.add("message", "strings");
            return rel;
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            DebugWebSocketServer.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }
}
