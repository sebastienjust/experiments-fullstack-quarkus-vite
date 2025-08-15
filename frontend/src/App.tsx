import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import {FirstComponent} from "./components/FirstComponent.tsx";

const initialDataJsonStr = document.getElementById("__INITIAL_DATA__")?.textContent
const initialData = initialDataJsonStr ? JSON.parse(initialDataJsonStr) : {}

function App() {
    return (
        <>
            <div>
                <a href="https://vite.dev" target="_blank">
                    <img src={viteLogo} className="logo" alt="Vite logo"/>
                </a>
                <a href="https://react.dev" target="_blank">
                    <img src={reactLogo} className="logo react" alt="React logo"/>
                </a>
            </div>
            <h1>Vite + React</h1>
            <FirstComponent message={"Initial data: " + JSON.stringify(initialData)}/>
        </>
    )
}

export default App
