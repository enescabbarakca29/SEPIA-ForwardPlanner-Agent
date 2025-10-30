# SEPIA Resource Collection Planner (Ileri Yapay Zeka Odevi)

## Ogrenci Bilgileri ve Teslimat
* Ad Soyad: Enes Cabbar AKCA
* Ogrenci ID: 23291189 


## Proje Amaci ve Uygulanan Algoritma
Bu proje, minimum Makespan planlarini bulmak icin bir Ileri Durum Uzayi Planlayicisi (Forward State Space Planner) kullanilarak bir kaynak toplama senaryosunu cözer.
* Algoritma: A* Arama Algoritmasi.

## Optimalite ve Strateji Aciklamasi

### 1. Heuristic Fonksiyonu (Optimalite icin Kritik Adim)

Heuristic fonksiyonumuzun amaci, BuildPeasant eyleminin anlik maliyetini (400 altin kaybi) uzun vadeli zaman kazancina karsi takas etmektir.

* Model: Kalan is yuku, mevcut koylu sayisina bolunerek hesaplanir: h(n) = (Toplam Kalan Is Yuku (Zaman)) / (Koylu Sayisi).
* Ayar: Is Yukunu (Zaman) belirleyen katsayi (avgTripCost) 30.0 olarak ayarlanmistir. Bu ayar, BuildPeasant eyleminin h(n) degerini (koylu sayisi artisindan dolayi) anlik altin kaybindan daha fazla dusurmesini saglar. Bu sayede A* algoritmasi, 400 altin kurali saglandiginda optimal karar olarak yatirim yapmayi secer.

### 2. Paralelizm ve Maliyet Modellemesi

* BuildPeasant Maliyeti: Planlayicida, BuildPeasant eyleminin zaman maliyeti g(n) icin 0.0 olarak kabul edilir. Bu, TownHall uretim yaparken diger koylulerin calismaya devam edebildigi varsayimini destekler.
* PEA Paralelizmi: Plan Yurutme Ajani (MidasAgent), plani uygularken elde ettigi Moves, Harvests ve Deposits eylemlerini es zamanli olarak yurutur.

### 3. Ajan Kontrolu ve Carpisma Onleme

* Verimli Eylem Uretimi: Planlayici, arama uzayini budamak icin sadece en yakin Altin Madeni ve en yakin Orman'a gitme eylemlerini uretir. Bu, ajanin "yakindaki kaynak varken uzaga gitme" sorununu onler.
* Gorevi Icra Kontrolu: PEA, bir koyluye Harvest veya Deposit komutu vermeden once, hedefe gercekten bitisik olup olmadigini kontrol eder. Bu sayede uzaktan eylem yapilmasi engellenir.

