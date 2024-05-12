import matplotlib.pyplot as plt
import csv
import os
from pathlib import Path

# путь к директории с CSV-файлами
directory_path = 'D:/Учебные материалы/#Универ/3-й курс/КомпСети/дз1/#new-local-output-after-fix'

# все CSV-файлы в директории
csv_files = [file for file in os.listdir(directory_path) if file.endswith('.csv')]

# график для каждого файла
for csv_file in csv_files:
    csv_file_path = Path(directory_path) / csv_file

    with open(csv_file_path, 'r') as file:
        reader = csv.reader(file)
        next(reader)
        data = list(reader)

    data_sizes = [int(row[0]) for row in data]
    avg_times = [float(row[1]) for row in data]

    plt.figure(figsize=(15, 15))

    plt.plot(data_sizes, avg_times, marker='o', linestyle='-', color='b')
    plt.title(f'Зависимость времени от числа байт ({csv_file})')
    plt.xlabel('DataSize (байт)')
    plt.ylabel('Среднее время (ns)')

    # сохранение графика с тем же именем, что и у файла CSV
    graph_file_path = Path(directory_path) / f'{csv_file_path.stem}_graph.png'
    plt.savefig(graph_file_path)
    plt.close()
