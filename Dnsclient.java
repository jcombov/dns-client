import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import com.sun.javafx.collections.MappingChange.Map;

import es.uvigo.det.ro.simpledns.*;

public class Dnsclient {
	private static int segundos=0;
	
	static TimerTask timerTask = new TimerTask() 
    { 
        public void run()  
        { 
            segundos++; 
        } 
    }; 
	
	public static void main(String[] args) throws Exception {
		String tipo = args[0];
		String IPServidor = args[1].trim();
		String IPServidoraux = args[1].trim();
		String NServidor;
		String NServidoraux = "";
		String NServidor2;
		RRType Rtp = RRType.A;
		String protocolo = "";
		int implementado=0;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000); //Hacemos un contador para el TTL
		AResourceRecord AAux = null;
		AResourceRecord ACache = null;
		NSResourceRecord NSCache = null;
		LinkedHashMap<String, Cache> cache = new LinkedHashMap<String, Cache>();
		

		
		
		int aux = 1;
		int repetir = 0;

		String text = null;
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			System.out.println("\nConsulta:\n");
			text = new String(scanner.nextLine());
			text = text.replaceAll("\"", " ").trim();
			String[] subcadena = text.split(" ");
			IPServidor = IPServidoraux;
			if (subcadena[0].equals("AAAA")) {
				Rtp = RRType.AAAA;
				implementado=1;
			}
			else if (subcadena[0].equals("A")) {
				Rtp = RRType.A;
				implementado=1;
			}
			else if (subcadena[0].equals("NS")) {
				Rtp = RRType.NS;
				implementado=1;
			}
			else
			{
				System.out.println("No implementado");
				implementado=0;
			}

			NServidor = subcadena[1].trim();
			NServidoraux = NServidor;
			NServidor2 = NServidor;
			IPServidor = args[1].trim();
			IPServidoraux = IPServidor;
			if(implementado==1) {
			if (tipo.equals("-t")) {
				System.out.println("TCP NO HA SIDO IMPLEMENTADO");
				tipo = "-u";

			}

			if (tipo.equals("-u")) { // Creamos el mensaje

				protocolo = "UDP";
				int a = 1;
				int b=0;
				if(cache.size()==0)b=1;
				while(b==0) {
					int contador=0;
				for (HashMap.Entry<String, Cache> entry : cache.entrySet()) { //Miramos si hay que eliminar alguna entrada en el cache
					
					
					Cache consulta=entry.getValue();
					if(cache!=null) {
						
					if(consulta.getNServidor()!=null)
					{
						NSResourceRecord NSR=consulta.getNServidor();
						if(NSR.getTTL()<=segundos)
						{
							System.out.println("Borrado");
							cache.remove(entry.getKey());
							break;
						}
					}
					if(consulta.getARR()!=null)
					{
						AResourceRecord AR=consulta.getARR();
						if(AR.getTTL()<=segundos)
						{
							System.out.println("Borrado");
							cache.remove(entry.getKey());
							break;
						}
						
					}
					contador++;
					if(contador==cache.size())b=1;					
					}
				}}
				
				if(cache.containsKey(NServidor))
				{
					if(cache.get(NServidor).getARR()==null && cache.get(NServidor).getNServidor()==null)
					{
						protocolo = "cache";
						a=0;
						System.out.println("Q: " + protocolo + " " + " " + Rtp + " " + NServidor);
						System.out.println("A: cache No hay respuesta");
					}
				}
				
				if(Rtp.equals(RRType.NS))
				{
					int i;
					if(cache.containsKey(NServidor.concat("0")))
					{
						protocolo = "cache";
						a=0;
						System.out.println("Q: " + protocolo + " " + " " + Rtp + " " + NServidor);
					}
					for(i=0; i<cache.size();i++) {
					if(cache.containsKey(NServidor.concat(Integer.toString(i))))
					{
						Cache consulta=cache.get(NServidor.concat(Integer.toString(i)));
						NSResourceRecord NSR=consulta.getNServidor();
						System.out.println("A: " + protocolo + " " + NSR.getRRType() + " " + NSR.getTTL() + " "
								+ NSR.getNS());
					}
					}
				}

				if (cache.containsKey(NServidor.trim())) // Miramos en la cache a ver si hubo alguna consulta a ese
															// servidor
				{
					
					Cache consulta = cache.get(NServidor);
					if (consulta.getNServidor() == null && consulta.getARR()!=null) // Si no tiene campo NS significa que es una respuesta
					{
					if (consulta.getIP().equals(IPServidor) && consulta.getRRType().equals(Rtp)) { // Se busca si se ha
																									// realizado esa
																									// consulta con el
																									// mismo RRType y IP
																									// de entrada

						
							protocolo = "cache";
							AResourceRecord AR = consulta.getARR();
							a = 0;
							System.out.println("Q: " + protocolo + " " + " " + Rtp + " " + NServidor);
							System.out.println("A: " + protocolo + " " + AR.getRRType() + " " + AR.getTTL() + " "
									+ AR.getAddress().toString().replaceAll("/", " ").trim());
						}
					}
				} else {
					if (cache != null) {
						for (Cache consulta : cache.values()) // Recorremos la cache a ver si la actual consulta ha sido
																// a alguno de los servidores dns intermedios previamente almacenados
						{
							if (consulta.getNServidor() != null) {
								if (consulta.getNServidor().getNS().toString().equals(NServidor)
										&& consulta.getRRType().equals(Rtp)
										&& consulta.getIP().trim().equals(IPServidoraux.trim())) {
									protocolo = "cache";
									AResourceRecord AR = consulta.getARR();
									a = 0;
									System.out.println("Q: " + protocolo + " " + " " + Rtp + " " + NServidor);
									System.out.println("A: " + protocolo + " " + AR.getRRType() + " " + AR.getTTL()
											+ " " + AR.getAddress().toString().replaceAll("/", " ").trim());

								}
							}
						}
					}
				}

				while (a != 0) {
					NServidor = NServidor2;
					Message mensaje = new Message(NServidor, Rtp, false);
					DatagramSocket udpcliente = new DatagramSocket();
					byte[] Msg = mensaje.toByteArray();
					DatagramPacket peticion;
					if (AAux != null) // Si se ha vuelto a aquí es porque se ha encontrado un servidor NS autoritario
					{

						peticion = new DatagramPacket(Msg, Msg.length, AAux.getAddress(), 53);
						IPServidor = AAux.getAddress().toString().replaceAll("/", " ").trim();
						NServidor = NServidor2;

					} else { // Solo se utiliza para averiguar la primera IP
						peticion = new DatagramPacket(Msg, Msg.length, InetAddress.getByName(IPServidoraux), 53);
						IPServidor = IPServidoraux;
					}
					AAux = null;
					udpcliente.send(peticion);

					byte[] bufer = new byte[5000];
					DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length);
					udpcliente.receive(respuesta);
					Message Rpt = new Message(respuesta.getData());
					while (Rpt.getAnswers().isEmpty()) // Hacemos consultas hasta recibir la direccion final
					{
						repetir = 0;
						if (Rpt.getNameServers().isEmpty()) // No hay respuestas si no devuelve ningun servidor dns
						{
							System.out.println("No hay respuesta");
							Cache colocar= new Cache(null,null,0,null, IPServidoraux);
							cache.put(NServidor2, colocar);
							a=0;
							break;
						}
						int cacheaux = 1;
						if (cache.isEmpty())
							cacheaux = 0;
						if (cacheaux == 1) {
							if (cache != null) {
								for (Cache consulta : cache.values()) // Recorremos la cache a ver si podemos obtener
																		// los datos del servidor de siguiente salto
								{
									if (consulta.getNServidor() != null) {
										NSResourceRecord NSR = null;

										for (ResourceRecord RRR : Rpt.getNameServers()) // Registros NS
										{

											NSR = (NSResourceRecord) RRR;
											break;
										}
										if (consulta.getNServidor().getNS().toString().trim()
												.equals(NSR.getNS().toString().trim())) {
											protocolo = "cache";
											AResourceRecord AR = consulta.getARR();
											a = 0;
											System.out.println("Q: " + protocolo + " " + Rtp + " " + NServidor);
											System.out.println("A: " + protocolo + " " + NSR.getRRType() + " "
													+ NSR.getTTL() + " " + NSR.getNS());
											System.out.println(
													"A: " + protocolo + " " + AR.getRRType() + " " + AR.getTTL() + " "
															+ AR.getAddress().toString().replaceAll("/", " ").trim());

											peticion = new DatagramPacket(Msg, Msg.length, AR.getAddress(), 53);
											udpcliente.send(peticion);

											bufer = new byte[5000];
											respuesta = new DatagramPacket(bufer, bufer.length);
											udpcliente.receive(respuesta);
											Rpt = new Message(respuesta.getData());
											IPServidor = AR.getAddress().toString().replaceAll("/", " ").trim();
											break;

										}

									}

								}
								cacheaux = 0; // Si se llega aqui es que no se han encontrado coincidencias
							}
						}
						if (cacheaux == 0) {
							protocolo = "UDP";
							System.out.println("Q: " + protocolo + " " + IPServidor + " " + Rtp + " " + NServidor);

							for (ResourceRecord RRR : Rpt.getNameServers()) // Registros NS
							{

								NSResourceRecord NSR = (NSResourceRecord) RRR;
								System.out.println("A: " + IPServidor + " " + NSR.getRRType() + " " + NSR.getTTL() + " "
										+ NSR.getNS());

								NServidoraux = NSR.getNS().toString();
								NSCache = NSR;

								break;
							}

							

							if (Rpt.getAdditonalRecords().isEmpty() && Rpt.getAnswers().isEmpty()) // Si no hay tipo A
																									// se
																									// vuelve
																									// a la direccion
																									// inicial y
																									// a preguntar a
																									// otro //
																									// servidor
							{
								repetir = 1;
								a = 2; // Ponemos este valor para hacer consulta sobre servidor autoritario
								NServidor = NServidoraux;
								mensaje = new Message(NServidoraux, Rtp, false);
								Msg = mensaje.toByteArray();
								peticion = new DatagramPacket(Msg, Msg.length, InetAddress.getByName(IPServidoraux),
										53);
								udpcliente.send(peticion);

								bufer = new byte[5000];
								respuesta = new DatagramPacket(bufer, bufer.length);
								udpcliente.receive(respuesta);
								Rpt = new Message(respuesta.getData());
								IPServidor = IPServidoraux;

							}
							if (repetir == 0) {
								for (ResourceRecord RR : Rpt.getAdditonalRecords()) { // Registros tipo A, se busca la
																						// IP de siguiente salto

									if (RR.getRRType().toString().trim().equals("A")) {

										
										AResourceRecord AR = (AResourceRecord) RR;
										if(NSCache.getNS().toString().trim().equals(AR.getDomain().toString().trim())) {
										System.out.println("A: " + IPServidor + " " + AR.getRRType() + " " + AR.getTTL()
												+ " " + AR.getAddress().toString().replaceAll("/", " "));
										peticion = new DatagramPacket(Msg, Msg.length, AR.getAddress(), 53);
										udpcliente.send(peticion);

										bufer = new byte[5000];
										respuesta = new DatagramPacket(bufer, bufer.length);
										udpcliente.receive(respuesta);
										Rpt = new Message(respuesta.getData());
										IPServidor = AR.getAddress().toString().replaceAll("/", " ").trim();
										ACache = AR;
										Cache colocar = new Cache(NSCache, ACache, 0, Rtp, IPServidoraux);
										cache.put(NSCache.getNS().toString().trim(), colocar);
										break;
										}
									}

								}

							}
						}

					}

					if (!Rpt.getAnswers().isEmpty()) {
						System.out.println("Q: " + protocolo + " " + IPServidor + " " + Rtp + " " + NServidor);
						int i=0; 
						for (ResourceRecord RR : Rpt.getAnswers()) {

							if (RR.getRRType().toString().trim().equals("CNAME")) {
								System.out.println("A: CNAME"); // Miramos las CNAME pero seguimos buscando por si hay
																// tipo A
							}
							if (RR.getRRType().toString().trim().equals("A")) {

								AResourceRecord AR = (AResourceRecord) RR;
								System.out.println("A: " + IPServidor + " " + AR.getRRType() + " " + AR.getTTL() + " "
										+ AR.getAddress().toString().replaceAll("/", " ").trim());

								if (a == 2) {
									AAux = AR;
									a = 1;
									break;
								}
								Cache colocar = new Cache(null, AR, 1, Rtp, IPServidoraux);
								cache.put(NServidor, colocar);
								a = 0; // Si hay tipo A se sale de la ejecución

							}
							
							if(RR.getRRType().toString().trim().equals("NS")) {
								
								NSResourceRecord NSR= (NSResourceRecord) RR;
								System.out.println("A: "+IPServidor+" " + NSR.getRRType() + " " + NSR.getTTL()+ " " + NSR.getNS() );
								Cache colocar = new Cache(NSR, null, 1, Rtp, IPServidoraux);
								
								cache.put(NServidor.concat(Integer.toString(i)), colocar);
								
								i++;
							}

							if (RR.getRRType().toString().trim().equals("AAAA")) {
								AAAAResourceRecord AAAAR = (AAAAResourceRecord) RR;
								System.out.println("A: " + IPServidor + " " + AAAAR.getRRType() + " " + AAAAR.getTTL()
										+ " " + AAAAR.getAddress().toString().replaceAll("/", " ").trim());
							}
							aux++;
							if (aux > Rpt.getAnswers().size()) {
								a = 0;
							}
						}
					}
					udpcliente.close();
				}
			}
		}
		}
		scanner.close();
		timer.cancel();
		return;
		
	}
	 
	
	
}
