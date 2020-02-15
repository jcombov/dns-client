import es.uvigo.det.ro.simpledns.AResourceRecord;
import es.uvigo.det.ro.simpledns.NSResourceRecord;
import es.uvigo.det.ro.simpledns.RRType;
import es.uvigo.det.ro.simpledns.ResourceRecord;

public class Cache {

	AResourceRecord ARR;
	int completada;
	NSResourceRecord NRR;
	RRType rtype;
	String IP;
	
	public Cache(NSResourceRecord NRR, AResourceRecord ARR,int  completada, RRType rtype, String IP)
	{
		this.NRR=NRR;
		this.ARR=ARR;
		this.completada=completada;
		this.rtype=rtype;
		this.IP=IP;
	}
	
	public NSResourceRecord getNServidor()
	{
		return NRR;
	}
	
	public AResourceRecord getARR()
	{
		return ARR;
	}
	
	public int getCompletada()
	{
		return completada;
	}
	public RRType getRRType()
	{
		return rtype;
	}
	public String getIP()
	{
		return IP;
	}
	
	
}
