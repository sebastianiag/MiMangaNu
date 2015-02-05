package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.LectureEnLigne;

public class LectureEnLigneTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new LectureEnLigne();
		manga = new Manga(server.getServerID(), "Abias", "http://www.lecture-en-ligne.com/manga/asura/", false);
		capitulo = new Capitulo("uno", "http://www.lecture-en-ligne.com/asura/3/0/0/1.html");
	}

}
