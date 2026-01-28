# ShapeSignatureApp 

**ShapeSignatureApp** es una aplicación de Android de alto rendimiento diseñada para reconocer formas geométricas básicas (Círculo, Cuadrado y Triángulo) dibujadas a mano alzada. Utiliza el poder de **OpenCV** y descriptores matemáticos avanzados para ofrecer una precisión excepcional.

## Características Principales

- **Reconocimiento en Tiempo Real:** Motor de procesamiento desarrollado en **C++ (NDK)** para una latencia mínima.
- **Invarianza Total:** Gracias a los Descriptores de Fourier, el sistema reconoce la forma sin importar su:
    - **Tamaño** (Escala).
    - **Posición** (Traslación).
    - **Orientación** (Rotación).
- **Lienzo Persistente:** `DrawingView` optimizado con un buffer de 1000x1000 px que evita la pérdida de trazos.
- **Sistema de Reportes:** Genera informes detallados en la carpeta pública `Download` con:
    - Imagen del dibujo capturado.
    - Clase predicha y porcentaje de confianza.
    - Distancia Euclídea respecto al dataset.
    - Vector completo del descriptor de Fourier.

## Arquitectura Técnica

### Flujo de Procesamiento (Core C++)

El núcleo de la lógica reside en `signature.cpp` y sigue este flujo matemático:

1.  **Segmentación:** Binarización mediante **Umbral Adaptativo** para aislar el trazo del fondo.
2.  **Extracción de Contornos:** Localización del contorno externo más relevante utilizando `cv::findContours`.
3.  **Remuestreo:** Normalización del contorno a exactamente 128 puntos para uniformidad estadística.
4.  **Señal Compleja:** Construcción de la señal $s(n) = (x(n) - x_c) + j(y(n) - y_c)$, donde $(x_c, y_c)$ es el centroide.
5.  **DFT (Transformada Discreta de Fourier):** Aplicación de `cv::dft` para pasar del dominio espacial al de frecuencia.
6.  **Normalización de Invarianza:**
    - Se descarta la fase para lograr invarianza a la **rotación**.
    - Se dividen los armónicos por $|F(1)|$ para lograr invarianza a la **escala**.
7.  **Clasificación:** Cálculo de la **Distancia Euclídea** entre los primeros 15 armónicos del dibujo y los promedios del corpus de entrenamiento.

### Componentes de Android (Kotlin)

- **JNI (Java Native Interface):** Puente de comunicación entre la UI y el motor de OpenCV en C++.
- **MediaStore API:** Gestión de archivos para guardar reportes en carpetas públicas de forma segura y compatible con Android 10+.
- **View Binding:** Para una interacción limpia y segura con los elementos de la interfaz.

## Metodología de Validación

El sistema fue validado mediante un estudio de **30 pruebas dirigidas**, alcanzando una precisión del **100%**. El sistema de confianza implementado utiliza la función:
$$Confianza = \frac{1}{1 + Distancia}$$
Esto permite cuantificar qué tan "parecido" es el dibujo al estándar matemático de la figura.

## 📂 Estructura del Proyecto

- `/app/src/main/cpp`: Motor de reconocimiento en C++ y puentes JNI.
- `/app/src/main/assets`: Dataset de entrenamiento (`class_averages.txt`).
- `/app/src/main/java`: Lógica de la aplicación y vista personalizada de dibujo.
- `/opencv`: Módulo del SDK de OpenCV para Android.

---
