import numpy as np
import pandas as pd


def gen_float(low, high, n_rows, n_cols=None):
    """Generate dataset randomly.
    Arguments:
        low {int} -- The minimum value of element generated.
        high {int} -- The maximum value of element generated.
        n_rows {int} -- Number of rows.
        n_cols {int} -- Number of columns.
    Returns:
        list -- 1d or 2d list with int
    """
    if n_cols is None:
        ret = [np.random.uniform(low, high) for _ in range(n_rows)]
    else:
        ret = [[np.random.uniform(low, high) for _ in range(n_cols)]
               for _ in range(n_rows)]
    return ret


def gen_int(low, high, n_rows, n_cols=None):
    """Generate dataset randomly.
    Arguments:
        low {int} -- The minimum value of element generated.
        high {int} -- The maximum value of element generated.
        n_rows {int} -- Number of rows.
        n_cols {int} -- Number of columns.
    Returns:
        list -- 1d or 2d list with int
    """
    if n_cols is None:
        ret = [np.random.random(low, high) for _ in range(n_rows)]
    else:
        ret = [[np.random.random(low, high) for _ in range(n_cols)]
               for _ in range(n_rows)]
    return ret


def gen_cat(n_rows, cat=["restaurant", "education", "hospital"]):
    RANDOM = np.random.choice(cat)
    ret = [np.random.choice(cat) for _ in range(n_rows)]
    return ret


def generate_fix_number_sample_list(N: int):
    SEED = 2021
    np.random.seed(SEED)
    lat_list = gen_float(-90, 90, N)
    lon_list = gen_float(-180, 180, N)
    id_list = ["id" + str(i) for i in range(N)]
    cat_list = gen_cat(N)
    d = {"id": id_list, "cat": cat_list, "lat": lat_list, "lon": lon_list}
    df = pd.DataFrame(data=d)
    filename = "sample_data_" + str(N)
    df.to_csv(path_or_buf=filename, sep=" ", header=False, index=False)
    return df


def generate_command(df, type: str, K: int, M: int):
    N = len(df)
    df = df.sample(n=M)
    df['k-number'] = K
    df['command'] = type
    df_out = df.drop(columns=['id'])

    # Get the DataFrame column names as a list
    clist = list(df_out.columns)

    # Rearrange list the way you like
    clist_new = clist[-1:] + clist[:-1]  # brings the last column in the first place
    # Pass the new list to the DataFrame - like a key list in a dict
    df_out = df_out[clist_new]

    filename = "test_{}_k{}_M{}.in".format(N, K, M)
    df_out.to_csv(path_or_buf=filename, sep=" ", header=False, index=False)


def generate_sample(N: int, K: int, M: int):
    df = generate_fix_number_sample_list(N)
    df = generate_add(df, N, M, K)
    generate_command(df, "S", K, M)


def generate_add_del(df, N, M, K):
    lat_list = gen_float(-90, 90, M)
    lon_list = gen_float(-180, 180, M)
    id_list = ["id" + str(i) for i in range(N + 1, N + 1 + M)]
    cat_list = gen_cat(M)
    d = {"id": id_list, "cat": cat_list, "lat": lat_list, "lon": lon_list}
    df_add = pd.DataFrame(data=d)
    result = pd.concat([df, df_add])

    df_add['command'] = "A"
    df_out = df_add[['command', 'id', 'cat', 'lat', 'lon']]
    filename = "dynamic_test_{}_k{}_M{}.in".format(N, K, M)
    df_out.to_csv(path_or_buf=filename, sep=" ", header=False, index=False, mode='w')

    df_del = result.sample(n=M)
    df_del['command'] = "D"
    df_out_d = df_del[['command', 'id', 'cat', 'lat', 'lon']]
    df_out_d.to_csv(path_or_buf=filename, sep=" ", header=False, index=False, mode='a')

    df_result = result.sample(n=M)
    df_result['k-number'] = K
    df_result['command'] = "S"
    df_result = df_result[['command', 'cat', 'lat', 'lon', 'k-number']]
    df_result.to_csv(path_or_buf=filename, sep=" ", header=False, index=False, mode='a')

    return df_result


def generate_dynamic_sample(N: int, K: int, M: int):
    df = generate_fix_number_sample_list(N)
    generate_add_del(df, N, M, K)


if __name__ == '__main__':
    generate_sample(1000, 1, 10)
    generate_sample(1000, 3, 10)
    generate_sample(1000, 7, 10)
    generate_sample(10000, 1, 10)
    generate_sample(10000, 3, 10)
    generate_sample(10000, 7, 10)
    generate_sample(100000, 1, 10)
    generate_sample(100000, 3, 10)
    generate_sample(100000, 7, 10)
    generate_dynamic_sample(1000, 5, 10)
    generate_dynamic_sample(1000, 5, 100)
    generate_dynamic_sample(1000, 5, 1000)
    generate_dynamic_sample(10000, 5, 10)
    generate_dynamic_sample(10000, 5, 100)
    generate_dynamic_sample(10000, 5, 1000)
    generate_dynamic_sample(100000, 5, 10)
    generate_dynamic_sample(100000, 5, 100)
    generate_dynamic_sample(100000, 5, 1000)
