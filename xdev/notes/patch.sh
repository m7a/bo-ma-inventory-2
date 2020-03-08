find . -type f -name '*.pm' -exec sed -i 's/addstr/addstring/g' {} \;
find . -type f -name '*.pm' -exec sed -i 's/getch/getchar/g'    {} \;
