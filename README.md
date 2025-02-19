## Karaoke Party
<p align="justify">Przedstawiony projekt to aplikacja internetowa wspomagająca organizowanie wydarzeń karaoke poprzez dynamiczną kolejkę piosenek aktualizowaną na bieżąco przez moderatora. Aplikacja została napisana w całości w języku Java z wykorzystaniem Frameworka Spring, a także takich bibliotek i frameworków jak między innymi <i>mysql-connector-j</i>, który odpowiada za obsługę połączenia z bazą danych MySQL, <i>spring-boot-starter-thymeleaf</i>, który jest silnikiem szablonów HTML, <i>spring-boot-starter-validation</i>, obsługujący walidację danych z użyciem odpowiednich adnotacji, <i>spring-boot-starter-test</i>, czyli Framework do testów integracyjnych i jednostkowych zawierający JUnit i AssertJ, <i>jakarta.xml.bind:jakarta.xml.bind-api</i>, który odpowiada za obsługę plików XML, a także <i>org.projectlombok:lombok</i>, czyli biblioteka umożliwiająca automatyczne generowanie kodu (np. @Getter, @Setter). Projekt ten zrealizowali:</p>

      Marek Prokopiuk
      Paweł Pieczykolan

---
<p align="justify">Aplikacja działa w następujący sposób. Użytkownik może się zarejestrować lub zalogować, jeżeli zarejestrował się już wcześniej. Próba wejścia niezalogowanego użytkownika, na jakikolwiek element strony, kończy się przekierowaniem do formularza logowania. Zalogowany zwykły użytkownik ma wgląd w listę piosenek, a także szczegóły każdej z nich. Oprócz tego może zobaczyć aktualną kolejkę karaoke i dodać swój utwór. Zwykły użytkownik może mieć w danej chwili tylko jedną dodaną przez siebie piosenkę w kolejce. Oprócz tego wyróżniany jest moderator. Może on aktualizować pozycje piosenek w kolejce, usuwać piosenki z kolejki, a także zmieniać utwory na określonych pozycjach. Może też dodawać nieograniczoną ilość piosenek do kolejki. Moderator może dodawać nowe piosenki do bazy danych, a także je edytować i usuwać. Wykorzystywana jest baza danych mysql, której obraz został pobrany z użyciem Dockera.</p>

<p align="justify">W aplikacji dostępne są formularze rejestracji oraz logowania. Na początku został utworzony model użytkownika, a także klasa przechowująca listę użytkowników zarejestrowanych. Model użytkownika zawiera takie pola jak login, hasło, numer telefonu, e-mail, sól oraz rolę. W modelu została zrealizowana walidacja. Login jest wymagany, musi być z zakresu od 5 do 15 znaków i nie może mieć trzech powtarzających się po sobie znaków (zastosowany został odpowiedni regex). Hasło jest wymagane, musi mieć co najmniej 8 znaków i zawierać przynajmniej jedną małą literę, jedną dużą literę, jedną cyfrę i jeden znak specjalny. Numer telefonu oraz e-mail nie są wymagane, ale jeżeli użytkownik postanowi je wpisać, to muszą zostać podane w odpowiednim formacie. Poza danymi wprowadzanymi przez użytkownika podczas rejestracji model zawiera również sól oraz rolę. Sól jest generowana dla każdego użytkownika podczas hashowania hasła, natomiast rolę można dodać w XML-u, dla wybranego użytkownika, aby uczynić go moderatorem.</p>

<p align="justify">Utworzony został kontroler odpowiedzialny za rejestrację użytkowników. Mapuje on konkretne endpointy, aby spowodowały wyświetlenie odpowiednich stron i przekazanie im potrzebnych argumentów. Następnie w przypadku, gdy użytkownik wprowadzi prawidłowe dane, kontroler wczytuje aktualną listę użytkowników z pliku XML i dopisuje do niej nowo zarejestrowaną osobę. Hasło użytkownika zapisywane w pliku jest hashowane. W tym celu wykorzystano klasę, która hashuje hasła algorytmem SHA-256, a także stałą wartością pieprzu oraz generowaną solą zapisywaną dla każdego użytkownika w celu późniejszej weryfikacji danych logowania.</p>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 1. Widok formularza rejestracji z walidacją danych</i>
</p><br>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 2. Widok strony głównej po zarejestrowaniu</i>
</p>

---
<p align="justify">Został utworzony kolejny kontroler odpowiadający za proces logowania, w którym sprawdzane jest, czy został przekroczony limit prób (użytkownik musi wówczas odczekać określoną ilość czasu), walidowane są dane pod kątem wymagalności, a następnie weryfikowane jest hasło dla użytkownika o danym loginie. W przypadku poprawnego zalogowania następuje przekierowanie na stronę główną aplikacji oraz ustawiane są odpowiednie atrybuty sesji informujące o zalogowaniu. W formularzach logowania i rejestracji zostały dodane przyciski umożliwiające płynne przechodzenie pomiędzy tymi formularzami.</p>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 3. Widok formularza logowania po podaniu dwa razy błędnych danych</i>
</p>

---
<p align="justify">Aplikacja zawiera dwa główne widoki, czyli listę piosenek i kolejkę karaoke. Klasa odpowiadająca za piosenki zawiera takie pola jak identyfikator, login użytkownika, który dodał piosenkę, autor, tytuł, gatunki (jako listę), kraj wydania oraz tekst piosenki, a także jej długość w sekundach. W klasie tej zostały dodane również odpowiednie adnotacje z biblioteki Lombok w celu zmniejszenia linii kodu oraz odpowiednie adnotacje walidujące. Przygotowana została również klasa będąca repozytorium odpowiadającym za komunikację z bazą danych i realizująca operacje CRUD na tabeli z piosenkami. Utworzono kontroler odpowiedzialny za mapowanie endpointów związanych z piosenkami i wyświetlanie odpowiednich formularzy.</p>

<p align="justify">Zwykły użytkownik ma dostęp tylko do wyświetlenia listy piosenek oraz szczegółów wybranej piosenki, natomiast moderator może dodawać piosenki, edytować je oraz usuwać. W formularzu dodawania piosenki trzeba podać tytuł utworu, wykonawcę oraz długość piosenki w sekundach. Można wprowadzić tekst utworu, ale jest to opcjonalne. Trzeba wybrać przynajmniej jeden gatunek z pola multiselect oraz kraj pochodzenia utworu jako jeden z dostępnych radiobuttonów. Edytowanie wybranej piosenki działa na tej samej zasadzie co dodawanie, lecz przy wejściu do formularza, wszystkie aktualne dane są już wprowadzone i zaznaczone.</p>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 4. Widok listy piosenek z perspektywy zwykłego użytkownika</i>
</p><br>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 5. Widok listy piosenek z perspektywy moderatora</i>
</p><br>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 6. Widok Formularza dodawania nowej piosenki (walidacja danych)</i>
</p>

---
<p align="justify">Utworzona została klasa modelowa odpowiadająca za kolejkę piosenek i zawierająca takie pola jak pozycja danej piosenki w kolejce, nazwa piosenki, login użytkownika, który dodał piosenkę do kolejki, a także id tej piosenki. W klasie tej zostały dodane również odpowiednie adnotacje z biblioteki Lombok. Zdefiniowano repozytorium odpowiedzialne za komunikację z bazą danych. Zostały przygotowane metody zwracające wszystkie piosenki z tabeli posortowane rosnąco względem pozycji, dodające nową pozycję do kolejki, usuwające piosenkę z kolejki na podstawie numeru porządkowego, wyszukujące pozycję w kolejce, sprawdzające, czy dany użytkownik już dodał piosenkę do kolejki, resetujące kolejkę, a także aktualizujące pozycje wszystkich elementów kolejki po dokonaniu jakieś zmiany w tabeli. Oprócz tego utworzona została klasa obsługująca REST API.</p>

<p align="justify">Zwykły użytkownik ma dostęp tylko do wyświetlenia kolejki piosenek, wejścia w szczegóły piosenki z kolejki oraz dodanie piosenki do kolejki, jeżeli nie ma aktualnie żadnej dodanej. Moderator natomiast może dodawać nieograniczoną ilość piosenek do kolejki, edytować nazwy piosenek na wybranych pozycjach lub pozycje dla wybranych piosenek w kolejce, a także może usuwać pozycje z kolejki.</p>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 7. Widok kolejki z perspektywy użytkownika zwykłego, mającego swoją piosenkę w kolejce</i>
</p><br>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 8. Widok kolejki z perspektywy moderatora</i>
</p><br>

<p align="center">
  <img src="" style="width: 80%; height: 80%" /></p>
<p align="center">
  <i>Rys. 9. Próba ustawienia niedozwolonej nowej pozycji </i>
</p>

---
<p align="justify">Zrealizowany projekt zawiera testy jednostkowe napisane z wykorzystaniem JUnit sprawdzające poprawność różnych elementów związanych z logowaniem, rejestracją, operacjami na piosenkach, czy aktualizowaniem pozycji piosenek w kolejce. Utworzona aplikacja do karaoke została napisana w języku Java, z wykorzystaniem Frameworka Spring, a także innych frameworków i bibliotek. Zostało zrealizowane połączenie i obsługa bazy danych, logowanie i rejestracja oraz hashowanie haseł z dołączeniem soli i pieprzu. Dodana została obsługa sesji użytkownika, a także operacje CRUD na tabeli z piosenkami oraz elementami kolejki. Umieszczone zostały rozbudowane formularze do zapisu oraz edycji danych wraz z odpowiednią walidacją. Zrealizowano obsługę ról użytkowników w aplikacji oraz dodano komponenty występujące jedynie dla moderatora.</p>
