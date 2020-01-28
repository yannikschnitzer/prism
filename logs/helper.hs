

justSeconds :: String -> String
justSeconds = takeWhile (/= 's' ) . drop 2 . dropWhile (/= ':') . tail . dropWhile (/= ':')

printAll :: String -> IO ()
printAll = sequence_ . map (putStrLn . justSeconds) . lines

main :: IO ()
main = do  std <- readFile "standard-times"
           lu  <- readFile "lower-upper-times"
           sou <- readFile "sound-times"
           opt <- readFile "optimistic-times"
           putStrLn "Standard"
           printAll std
           putStrLn "Lower-Upper"
           printAll lu
           putStrLn "Sound"
           printAll sou
           putStrLn "Optimistic"
           printAll opt


