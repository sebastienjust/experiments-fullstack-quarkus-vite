import {useState} from "react";

export function FirstComponent({message}: { message: string }) {
    const [count, setCount] = useState(0)
    return <div>
        <div style={{border: "1px solid red", padding: "1em"}}>{message}</div>
        <button onClick={() => setCount((count) => count + 1)}>
            count is {count}
        </button>
    </div>
}