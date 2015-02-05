package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.KissManga;

public class KissMangaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new KissManga();
		manga = new Manga(server.getServerID(), "Abias", "/Manga/A-Bias-Girl", false);
		capitulo = new Capitulo("uno", "/Manga/A-Bias-Girl/Vol-001-Ch-005-Read-Online?id=105883");
	}

}
