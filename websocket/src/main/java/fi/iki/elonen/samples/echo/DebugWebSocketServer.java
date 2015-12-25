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
                String smsg=null;
                if (method.contains("User")) {
                	smsg = dealWithUser(method);
                }
                else if (method.contains("Model")){
                	smsg  = dealWithModel(method);
                }
                else if (method.contains("Property")){
                	smsg  = dealWithProperty(method);
                }
                else if (method.contains("Task")){
                	smsg  = dealWithTask(method);
                }
                else 
                	smsg  = dealWithOthers(method);
                if ( smsg != null ) {
                	message.setTextPayload(smsg);
                	sendFrame(message);
                    System.out.println(textmsg);
                }
                // ignore repeating "breathe" message
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected String dealWithUser (String method)
        {
        	String smsg = null;
            if (method.equals("createUser")) {
            	smsg="{\"method\" : \"answerCreateUser\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
            } 
            else if (method.equals("saveUser")){
               	smsg="{\"method\" : \"answerSaveUser\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
                } 
            else if (method.equals("listUser")){
               	smsg="{\"method\" : \"provideListUser\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else if (method.equals("requireUser")){
               	smsg="{\"method\" : \"provideUser\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else smsg="{\"method\" : \"answerRemoveUser\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
            return smsg;     
        }
        
        protected String dealWithModel(String method)
        {
        	String smsg = null;
            if (method.equals("createModel")) {
            	smsg="{\"method\" : \"answerCreateModel\",\"status\" : false|true,\"message\" :<why failed|empty string>,\"id\" : <id>}";
            } 
            else if (method.equals("saveModel")){
               	smsg="{\"method\" : \"answerSaveModel\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
                } 
            else if (method.equals("listModel")){
               	smsg="{\"method\" : \"provideListModel\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else if (method.equals("requireModel")){
               	smsg="{\"method\" : \"provideModel\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else smsg="{\"method\" : \"answerRemoveModel\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
            return smsg;     
        }
        
        protected String dealWithProperty(String method)
        {
        	String smsg = null;
            if (method.equals("createProperty")) {
            	smsg="{\"method\" : \"answerCreateProperty\",\"status\" : false|true,\"message\" :<why failed|empty string>,\"id\" : <id>}";
            } 
            else if (method.equals("saveProperty")){
               	smsg="{\"method\" : \"answerSaveProperty\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
                } 
            else if (method.equals("listProperty")){
               	smsg="{\"method\" : \"provideListProperty\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else if (method.equals("requireProperty")){
               	smsg="{\"method\" : \"provideProperty\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else smsg="{\"method\" : \"answerRemoveProperty\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
            return smsg;     
        }
        
        protected String dealWithTask(String method)
        {
        	String smsg = null;
            if (method.equals("createTask")) {
            	smsg="{\"method\" : \"answerCreateTask\",\"status\" : false|true,\"message\" :<why failed|empty string>,\"id\" : <id>}";
            } 
            else if (method.equals("listTask")){
               	smsg="{\"method\" : \"provideListTask\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else if (method.equals("requireTask")){
               	smsg="{\"method\" : \"provideTask\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...]}";
                } 
            else smsg="{\"method\" : \"answerRemoveTask\",\"status\" : false|true,\"message\" :<why failed|empty string>}";
            return smsg;     
        }
  
        protected String dealWithOthers (String method){
        	String smsg = null;
        	if (method.equals("login"))
        	{
               	smsg="{\"method\" : \"provideSession\",\"status\" : false|true,\"message\" :<why failed|empty string>\"desc\" : [{ \"id\" :\"id\", ... /* all fields */ },...], \"session\":<session>}";       		
        	}
        	else if (method.equals(	"renewsession"))
        	{
        		smsg="{provideSession}";
        	}
        	// to keep active of the websocket
        	else if (method.equals("breathe") )
        	{
        		smsg=null;
        	}
        	else 
        		{smsg="unknown method: " + method;}
        	return smsg;
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
