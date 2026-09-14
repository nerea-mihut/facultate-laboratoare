import java.util.List;

public class StudentController {
    private StudentModel model;
    private StudentView view;

    public StudentController(StudentModel model, StudentView view) {
        this.model = model;
        this.view = view;
    }

    // Controlează fluxul datelor: citește din Model și trimite în View 
    public void pornesteAplicatie() {
        List<String> listaStudenti = model.citesteStudenti();
        view.afiseazaStudenti(listaStudenti);
    }
}