/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI_RegistroCivil;

import Enums.EstadoCivil;
import colecciones.Chileno;
import colecciones.Ciudadano;
import colecciones.Poblacion;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utilidades.ArchivoProperties;

/**
 *
 * @author Jean
 */
public class Registrar_Defuncion {
    private final String horaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    private TextArea logReporte;
    private ArchivoProperties prop;
    private Poblacion poblacion;
    private Chileno aux;
    
    public Registrar_Defuncion(TextArea logReporte, Poblacion poblacion, ArchivoProperties prop) {
        this.logReporte = logReporte;
        this.poblacion = poblacion;
        this.prop = prop;
    }
    
    public void registrarDefuncion(MouseEvent click){
        
        Stage ventana = new Stage();
        ventana.setX(370);
        ventana.setY(80);
        ventana.setResizable(false);
        ventana.initStyle(StageStyle.UTILITY);
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setWidth(600);
        ventana.setHeight(650);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(50,50,50,50));

        Button guardar = new Button("guardar");
        guardar.setDisable(true);
        Button cancelar = new Button("cancelar");
        cancelar.setOnMouseClicked(lambda->ventana.close());
        
        StackPane checkRut = Elementos.checkRut();
        TextField rut = (TextField)checkRut.getChildrenUnmodifiable().get(0);
        ImageView check = (ImageView)checkRut.getChildrenUnmodifiable().get(1);
        ImageView mark = (ImageView)checkRut.getChildrenUnmodifiable().get(2);
        GridPane registro = registroDatos(guardar, rut);
        rut.textProperty().addListener((observable, o, n)->{
            if(n.length()<8){
                check.setVisible(false);
                mark.setVisible(false);
            }
            else if(Chileno.comprobarRut(n) && poblacion.getPoblacion().containsKey(n)){
                    if(poblacion.getPoblacion().get(n).getDefuncion() == null){
                        mark.setVisible(false);
                        aux = (Chileno)poblacion.getPoblacion().get(n);
                        check.setVisible(true);
                        registro.setVisible(true);
                    }
                    else{
                        check.setVisible(false);
                        registro.setVisible(false);
                        mark.setVisible(true);
                    }
            }            
            else if(!Chileno.comprobarRut(n) || Chileno.comprobarRut(o)){
                check.setVisible(false);
                mark.setVisible(false);
            }
        });
        
        Label persona = new Label("");
        persona.setFont(Font.font("bold", FontWeight.NORMAL, 22));
        
        check.visibleProperty().addListener((obs, o, n)->{
            if(n.booleanValue()){
                this.aux = (Chileno)poblacion.getPoblacion().get(rut.getText());
                persona.setText(aux.getNombre()+" "+aux.getApellido());
            }
            else{
                this.aux = null;
                persona.setText("");
            }
        });
        
        HBox barra = new HBox(20, guardar, cancelar);
        barra.setAlignment(Pos.CENTER);
        
        grid.add(checkRut,0,0);
        grid.add(persona, 0, 1);
        grid.add(registro, 0, 2);
        grid.add(barra,0,3);
        
        Scene scene = new Scene(grid);
        scene.getStylesheets().add(prop.getProp().getProperty("tema_actual"));
        ventana.setScene(scene);
        ventana.show();
    }
    
    public GridPane registroDatos(Button guardar, TextField rut){
        GridPane datos = new GridPane();
        datos.setHgap(10);
        datos.setVgap(10);
        datos.setPadding(new Insets(5,5,5,5));
        datos.setAlignment(Pos.TOP_CENTER);
        StackPane fecha = new StackPane();
        DatePicker dia = new DatePicker();
        dia.setMaxSize(200, 40);
        dia.setPromptText("fecha de muerte");
        dia.setTranslateX(-100);
        Spinner<LocalTime> hora = Elementos.hora("hora de muerte");
        hora.setTranslateX(100);
        hora.setTranslateY(5);
        fecha.getChildren().addAll(dia, hora);
        
        TextArea comentario = new TextArea();
        comentario.setWrapText(true);
        comentario.setPromptText("comentarios");
        comentario.setMinSize(200, 300);
        datos.add(fecha, 0, 0);
        datos.add(comentario, 0, 1);
        datos.setVisible(false);
        
        BooleanBinding validacion = 
                dia.valueProperty().isNull().or(
                hora.valueProperty().isNull()
                );
        
        guardar.disableProperty().bind(validacion);
        guardar.setOnMouseClicked(lambda -> {
            aux.setDefuncion(dia.getValue());
            aux.setHoraDefuncion(hora.getValue().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            aux.setComentarioDefuncion(comentario.getText());
            if(aux.getParientes().buscarListaParentesco(EstadoCivil.CASADO)!=null){
                Ciudadano conyuge = aux.getParientes().ObtenerCiudadanoPorEstado(EstadoCivil.CASADO, 0);
                conyuge.setEstadoCivil(EstadoCivil.VIUDO);
                conyuge.getParientes().agregarPariente(aux, EstadoCivil.VIUDO);
            }
            logReporte.appendText(
                    "["+horaActual+"]"+aux.getNombre().toLowerCase()+" "+aux.getApellido().toLowerCase()+
                    ", rut: "+aux.getRut()+" QDEP\n");
            Elementos.popMensaje("Operacion Exitosa!", 300, 100);
            rut.clear();
            datos.setVisible(false);
        });       
        return datos;
    }
}